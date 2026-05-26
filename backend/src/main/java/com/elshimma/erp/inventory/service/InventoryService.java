package com.elshimma.erp.inventory.service;

import com.elshimma.erp.inventory.dto.*;
import com.elshimma.erp.inventory.entity.*;
import com.elshimma.erp.inventory.exception.ConcurrentInventoryModificationException;
import com.elshimma.erp.inventory.exception.InsufficientStockException;
import com.elshimma.erp.inventory.repository.*;
import com.elshimma.erp.product.entity.ProductVariant;
import com.elshimma.erp.product.exception.DuplicateResourceException;
import com.elshimma.erp.product.exception.ResourceNotFoundException;
import com.elshimma.erp.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductVariantRepository variantRepository;

    // ═══════════════════════════════════════════════════════════════
    //  INVENTORY CRUD
    // ═══════════════════════════════════════════════════════════════

    /**
     * Creates an inventory record linking a variant to a warehouse.
     * If initial quantity > 0, an IN movement is logged automatically.
     */
    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        // Validate variant exists
        ProductVariant variant = variantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductVariant", "id", request.getProductVariantId()));

        // Validate warehouse exists
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warehouse", "id", request.getWarehouseId()));

        // Prevent duplicate (variant + warehouse) combination
        if (inventoryRepository.existsByProductVariantIdAndWarehouseId(
                request.getProductVariantId(), request.getWarehouseId())) {
            throw new DuplicateResourceException(
                    "Inventory", "variant+warehouse",
                    request.getProductVariantId() + "+" + request.getWarehouseId());
        }

        BigDecimal initialQty = request.getCurrentQuantity() != null
                ? request.getCurrentQuantity() : BigDecimal.ZERO;
        BigDecimal minQty = request.getMinimumQuantity() != null
                ? request.getMinimumQuantity() : BigDecimal.ZERO;

        Inventory inventory = Inventory.builder()
                .productVariant(variant)
                .warehouse(warehouse)
                .currentQuantity(initialQty)
                .reservedQuantity(BigDecimal.ZERO)
                .minimumQuantity(minQty)
                .active(true)
                .build();

        Inventory saved = inventoryRepository.save(inventory);

        // Log initial stock as IN movement if quantity > 0
        if (initialQty.compareTo(BigDecimal.ZERO) > 0) {
            logMovement(saved, MovementType.IN, initialQty,
                    BigDecimal.ZERO, initialQty,
                    "Initial stock", null, "Inventory created with initial stock");
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getInventories(
            String keyword, Long warehouseId,
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Inventory> inventories = inventoryRepository.findWithFilters(keyword, warehouseId, pageable);
        return inventories.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(Long id) {
        return mapToResponse(findInventoryOrThrow(id));
    }

    @Transactional
    public InventoryResponse updateInventory(Long id, UpdateInventoryRequest request) {
        Inventory inventory = findInventoryOrThrow(id);

        if (request.getMinimumQuantity() != null) {
            inventory.setMinimumQuantity(request.getMinimumQuantity());
        }
        if (request.getActive() != null) {
            inventory.setActive(request.getActive());
        }

        return mapToResponse(inventoryRepository.save(inventory));
    }

    // ═══════════════════════════════════════════════════════════════
    //  STOCK OPERATIONS — The core of inventory management
    // ═══════════════════════════════════════════════════════════════

    /**
     * Adds stock to inventory.
     * Used when: receiving yarn shipments, production outputs fabric, etc.
     *
     * Flow:
     * 1. Load inventory (pessimistic read not needed — only increases)
     * 2. Record previous quantity
     * 3. Add to currentQuantity
     * 4. Save inventory
     * 5. Log the IN movement
     *
     * If step 5 fails → entire transaction rolls back → quantity reverted.
     */
    @Transactional
    public InventoryResponse addStock(Long inventoryId, StockMovementRequest request) {
        Inventory inventory = findInventoryForUpdateOrThrow(inventoryId);
        BigDecimal previousQty = inventory.getCurrentQuantity();
        BigDecimal newQty = previousQty.add(request.getQuantity());

        inventory.setCurrentQuantity(newQty);
        inventoryRepository.save(inventory);

        logMovement(inventory, MovementType.IN, request.getQuantity(),
                previousQty, newQty,
                request.getReason(), request.getReferenceNumber(), request.getNotes());

        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryResponse receiveStock(Long productVariantId, Long warehouseId, StockMovementRequest request) {
        Inventory inventory = findOrCreateInventory(productVariantId, warehouseId);
        return addStock(inventory.getId(), request);
    }

    @Transactional
    public void transferStock(Long productVariantId, Long sourceWarehouseId, Long destinationWarehouseId,
                              StockMovementRequest request) {
        Inventory sourceInventory = inventoryRepository
                .findByProductVariantIdAndWarehouseId(productVariantId, sourceWarehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory", "variant+warehouse", productVariantId + "+" + sourceWarehouseId));

        removeStock(sourceInventory.getId(), StockMovementRequest.builder()
                .quantity(request.getQuantity())
                .reason(request.getReason())
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .build());

        Inventory destinationInventory = findOrCreateInventory(productVariantId, destinationWarehouseId);
        addStock(destinationInventory.getId(), StockMovementRequest.builder()
                .quantity(request.getQuantity())
                .reason(request.getReason())
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .build());
    }

    /**
     * Removes stock from inventory.
     * Used when: fabric sent to dyeing, uniforms shipped to client, etc.
     *
     * CRITICAL RULE: Quantity can NEVER become negative.
     * Checks available quantity (current - reserved) before allowing removal.
     *
     * If insufficient stock → throws InsufficientStockException → rolls back.
     */
    @Transactional
    public InventoryResponse removeStock(Long inventoryId, StockMovementRequest request) {
        Inventory inventory = findInventoryForUpdateOrThrow(inventoryId);
        BigDecimal previousQty = inventory.getCurrentQuantity();
        BigDecimal available = inventory.getAvailableQuantity();

        if (available.compareTo(request.getQuantity()) < 0) {
            throw new InsufficientStockException(
                    inventoryId,
                    request.getQuantity().toPlainString(),
                    available.toPlainString());
        }

        BigDecimal newQty = previousQty.subtract(request.getQuantity());
        inventory.setCurrentQuantity(newQty);
        inventoryRepository.save(inventory);

        logMovement(inventory, MovementType.OUT, request.getQuantity(),
                previousQty, newQty,
                request.getReason(), request.getReferenceNumber(), request.getNotes());

        return mapToResponse(inventory);
    }

    /**
     * Reserves stock for a pending order.
     * Reserved stock is still physically in the warehouse but cannot be sold to others.
     *
     * Available = current - reserved. Reserve checks available quantity.
     */
    @Transactional
    public InventoryResponse reserveStock(Long inventoryId, StockMovementRequest request) {
        Inventory inventory = findInventoryForUpdateOrThrow(inventoryId);
        BigDecimal available = inventory.getAvailableQuantity();

        if (available.compareTo(request.getQuantity()) < 0) {
            throw new InsufficientStockException(
                    inventoryId,
                    request.getQuantity().toPlainString(),
                    available.toPlainString());
        }

        BigDecimal previousReserved = inventory.getReservedQuantity();
        inventory.setReservedQuantity(previousReserved.add(request.getQuantity()));
        inventoryRepository.save(inventory);

        logMovement(inventory, MovementType.RESERVED, request.getQuantity(),
                previousReserved, inventory.getReservedQuantity(),
                request.getReason(), request.getReferenceNumber(), request.getNotes());

        return mapToResponse(inventory);
    }

    /**
     * Releases previously reserved stock back to available.
     * Used when: an order is cancelled, or reserved stock is no longer needed.
     */
    @Transactional
    public InventoryResponse releaseStock(Long inventoryId, StockMovementRequest request) {
        Inventory inventory = findInventoryForUpdateOrThrow(inventoryId);
        BigDecimal currentReserved = inventory.getReservedQuantity();

        if (currentReserved.compareTo(request.getQuantity()) < 0) {
            throw new InsufficientStockException(
                    "Cannot release " + request.getQuantity().toPlainString()
                            + " — only " + currentReserved.toPlainString() + " reserved");
        }

        BigDecimal previousReserved = inventory.getReservedQuantity();
        inventory.setReservedQuantity(currentReserved.subtract(request.getQuantity()));
        inventoryRepository.save(inventory);

        logMovement(inventory, MovementType.RELEASED, request.getQuantity(),
                previousReserved, inventory.getReservedQuantity(),
                request.getReason(), request.getReferenceNumber(), request.getNotes());

        return mapToResponse(inventory);
    }

    // ═══════════════════════════════════════════════════════════════
    //  QUERIES
    // ═══════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getLowStockItems(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("currentQuantity").ascending());
        return inventoryRepository.findLowStockItems(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<InventoryMovementResponse> getMovementHistory(
            Long inventoryId, MovementType movementType,
            LocalDateTime from, LocalDateTime to,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InventoryMovement> movements = movementRepository.findWithFilters(
                inventoryId, movementType, from, to, pageable);
        return movements.map(this::mapToMovementResponse);
    }

    // ═══════════════════════════════════════════════════════════════
    //  WAREHOUSE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
        if (warehouseRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Warehouse", "name", request.getName());
        }

        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .active(true)
                .build();

        return mapToWarehouseResponse(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public Page<WarehouseResponse> getWarehouses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return warehouseRepository.findByActiveTrue(pageable).map(this::mapToWarehouseResponse);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    private Inventory findInventoryOrThrow(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
    }

    private Inventory findInventoryForUpdateOrThrow(Long id) {
        try {
            return inventoryRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        } catch (PessimisticLockingFailureException exception) {
            throw new ConcurrentInventoryModificationException(id);
        }
    }

    private Inventory findOrCreateInventory(Long productVariantId, Long warehouseId) {
        return inventoryRepository.findByProductVariantIdAndWarehouseId(productVariantId, warehouseId)
                .orElseGet(() -> {
                    ProductVariant variant = variantRepository.findById(productVariantId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "ProductVariant", "id", productVariantId));
                    Warehouse warehouse = warehouseRepository.findById(warehouseId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Warehouse", "id", warehouseId));

                    Inventory inventory = Inventory.builder()
                            .productVariant(variant)
                            .warehouse(warehouse)
                            .currentQuantity(BigDecimal.ZERO)
                            .reservedQuantity(BigDecimal.ZERO)
                            .minimumQuantity(BigDecimal.ZERO)
                            .active(true)
                            .build();

                    return inventoryRepository.save(inventory);
                });
    }

    /**
     * Creates an immutable movement log entry.
     * This is the core audit mechanism — every stock change is recorded.
     */
    private void logMovement(Inventory inventory, MovementType type,
                             BigDecimal quantity, BigDecimal previousQty, BigDecimal newQty,
                             String reason, String referenceNumber, String notes) {

        InventoryMovement movement = InventoryMovement.builder()
                .inventory(inventory)
                .movementType(type)
                .quantity(quantity)
                .previousQuantity(previousQty)
                .newQuantity(newQty)
                .reason(reason)
                .referenceNumber(referenceNumber)
                .notes(notes)
                .build();

        movementRepository.save(movement);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        ProductVariant variant = inventory.getProductVariant();
        Warehouse warehouse = inventory.getWarehouse();

        return InventoryResponse.builder()
                .id(inventory.getId())
                .productVariantId(variant.getId())
                .variantSku(variant.getSku())
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : null)
                .color(variant.getColor())
                .size(variant.getSize())
                .warehouseId(warehouse.getId())
                .warehouseName(warehouse.getName())
                .currentQuantity(inventory.getCurrentQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .minimumQuantity(inventory.getMinimumQuantity())
                .lowStock(inventory.isLowStock())
                .active(inventory.isActive())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    private InventoryMovementResponse mapToMovementResponse(InventoryMovement movement) {
        Inventory inventory = movement.getInventory();
        ProductVariant variant = inventory.getProductVariant();

        return InventoryMovementResponse.builder()
                .id(movement.getId())
                .inventoryId(inventory.getId())
                .movementType(movement.getMovementType())
                .quantity(movement.getQuantity())
                .previousQuantity(movement.getPreviousQuantity())
                .newQuantity(movement.getNewQuantity())
                .reason(movement.getReason())
                .referenceNumber(movement.getReferenceNumber())
                .notes(movement.getNotes())
                .createdAt(movement.getCreatedAt())
                .variantSku(variant.getSku())
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : null)
                .warehouseName(inventory.getWarehouse().getName())
                .build();
    }

    private WarehouseResponse mapToWarehouseResponse(Warehouse warehouse) {
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .active(warehouse.isActive())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }
}
