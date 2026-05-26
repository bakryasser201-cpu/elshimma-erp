package com.elshimma.erp.warehouse.service;

import com.elshimma.erp.inventory.dto.StockMovementRequest;
import com.elshimma.erp.inventory.entity.Inventory;
import com.elshimma.erp.inventory.entity.Warehouse;
import com.elshimma.erp.inventory.exception.InsufficientStockException;
import com.elshimma.erp.inventory.repository.InventoryRepository;
import com.elshimma.erp.inventory.repository.WarehouseRepository;
import com.elshimma.erp.inventory.service.InventoryService;
import com.elshimma.erp.product.entity.ProductVariant;
import com.elshimma.erp.product.exception.DuplicateResourceException;
import com.elshimma.erp.product.exception.ResourceNotFoundException;
import com.elshimma.erp.product.repository.ProductVariantRepository;
import com.elshimma.erp.warehouse.dto.*;
import com.elshimma.erp.warehouse.entity.*;
import com.elshimma.erp.warehouse.exception.InvalidWarehouseTransferStateException;
import com.elshimma.erp.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseTransferService {

    private final WarehouseLocationRepository locationRepository;
    private final WarehouseTransferRepository transferRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    @Transactional
    public WarehouseLocationResponse createLocation(WarehouseLocationRequest request) {
        Warehouse warehouse = findWarehouseOrThrow(request.getWarehouseId());
        if (locationRepository.existsByWarehouseIdAndBinCodeIgnoreCase(warehouse.getId(), request.getBinCode())) {
            throw new DuplicateResourceException("WarehouseLocation", "binCode", request.getBinCode());
        }
        WarehouseLocation location = WarehouseLocation.builder()
                .warehouse(warehouse)
                .aisle(request.getAisle())
                .shelf(request.getShelf())
                .binCode(request.getBinCode())
                .build();
        return mapToLocationResponse(locationRepository.save(location));
    }

    @Transactional(readOnly = true)
    public Page<WarehouseLocationResponse> getLocations(Long warehouseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("binCode").ascending());
        Page<WarehouseLocation> locations = warehouseId != null
                ? locationRepository.findByWarehouseId(warehouseId, pageable)
                : locationRepository.findAll(pageable);
        return locations.map(this::mapToLocationResponse);
    }

    @Transactional
    public WarehouseTransferResponse createTransfer(WarehouseTransferRequest request) {
        if (request.getSourceWarehouseId().equals(request.getDestinationWarehouseId())) {
            throw new InvalidWarehouseTransferStateException("Source and destination warehouses must be different");
        }

        Warehouse source = findWarehouseOrThrow(request.getSourceWarehouseId());
        Warehouse destination = findWarehouseOrThrow(request.getDestinationWarehouseId());

        WarehouseTransfer transfer = WarehouseTransfer.builder()
                .sourceWarehouse(source)
                .destinationWarehouse(destination)
                .status(WarehouseTransferStatus.PENDING)
                .requestedBy(request.getRequestedBy())
                .build();

        for (WarehouseTransferItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemRequest.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", itemRequest.getProductVariantId()));
            validateAvailableQuantity(variant.getId(), source.getId(), itemRequest.getQuantity());
            transfer.getItems().add(WarehouseTransferItem.builder()
                    .transfer(transfer)
                    .productVariant(variant)
                    .quantity(itemRequest.getQuantity())
                    .build());
        }

        return mapToTransferResponse(transferRepository.save(transfer));
    }

    @Transactional(readOnly = true)
    public Page<WarehouseTransferResponse> getTransfers(Long warehouseId, WarehouseTransferStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WarehouseTransfer> transfers;
        if (status != null) {
            transfers = transferRepository.findByStatus(status, pageable);
        } else if (warehouseId != null) {
            transfers = transferRepository.findBySourceWarehouseIdOrDestinationWarehouseId(warehouseId, warehouseId, pageable);
        } else {
            transfers = transferRepository.findAll(pageable);
        }
        return transfers.map(this::mapToTransferResponse);
    }

    @Transactional
    public WarehouseTransferResponse approveTransfer(Long id, String approvedBy) {
        WarehouseTransfer transfer = findTransferOrThrow(id);
        if (transfer.getStatus() != WarehouseTransferStatus.PENDING) {
            throw new InvalidWarehouseTransferStateException("Only PENDING transfers can be approved");
        }
        transfer.getItems().forEach(item -> validateAvailableQuantity(
                item.getProductVariant().getId(),
                transfer.getSourceWarehouse().getId(),
                item.getQuantity()));
        transfer.setStatus(WarehouseTransferStatus.IN_TRANSIT);
        transfer.setApprovedBy(approvedBy);
        return mapToTransferResponse(transferRepository.save(transfer));
    }

    @Transactional
    public WarehouseTransferResponse completeTransfer(Long id) {
        WarehouseTransfer transfer = findTransferOrThrow(id);
        if (transfer.getStatus() != WarehouseTransferStatus.IN_TRANSIT) {
            throw new InvalidWarehouseTransferStateException("Only IN_TRANSIT transfers can be completed");
        }

        for (WarehouseTransferItem item : transfer.getItems()) {
            inventoryService.transferStock(
                    item.getProductVariant().getId(),
                    transfer.getSourceWarehouse().getId(),
                    transfer.getDestinationWarehouse().getId(),
                    StockMovementRequest.builder()
                            .quantity(item.getQuantity())
                            .reason("Warehouse transfer")
                            .referenceNumber("WT-" + transfer.getId())
                            .notes("Transfer from " + transfer.getSourceWarehouse().getName()
                                    + " to " + transfer.getDestinationWarehouse().getName())
                            .build());
        }

        transfer.setStatus(WarehouseTransferStatus.COMPLETED);
        return mapToTransferResponse(transferRepository.save(transfer));
    }

    @Transactional
    public WarehouseTransferResponse cancelTransfer(Long id) {
        WarehouseTransfer transfer = findTransferOrThrow(id);
        if (transfer.getStatus() == WarehouseTransferStatus.COMPLETED) {
            throw new InvalidWarehouseTransferStateException("Completed transfers cannot be cancelled");
        }
        transfer.setStatus(WarehouseTransferStatus.CANCELLED);
        return mapToTransferResponse(transferRepository.save(transfer));
    }

    private Warehouse findWarehouseOrThrow(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
    }

    private WarehouseTransfer findTransferOrThrow(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseTransfer", "id", id));
    }

    private void validateAvailableQuantity(Long productVariantId, Long sourceWarehouseId, java.math.BigDecimal quantity) {
        Inventory inventory = inventoryRepository.findByProductVariantIdAndWarehouseId(productVariantId, sourceWarehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "variant+warehouse", productVariantId + "+" + sourceWarehouseId));
        if (inventory.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new InsufficientStockException(inventory.getId(), quantity.toPlainString(), inventory.getAvailableQuantity().toPlainString());
        }
    }

    private WarehouseLocationResponse mapToLocationResponse(WarehouseLocation location) {
        return WarehouseLocationResponse.builder()
                .id(location.getId())
                .warehouseId(location.getWarehouse().getId())
                .warehouseName(location.getWarehouse().getName())
                .aisle(location.getAisle())
                .shelf(location.getShelf())
                .binCode(location.getBinCode())
                .build();
    }

    private WarehouseTransferResponse mapToTransferResponse(WarehouseTransfer transfer) {
        List<WarehouseTransferItemResponse> items = transfer.getItems().stream()
                .map(this::mapToTransferItemResponse)
                .collect(Collectors.toList());
        return WarehouseTransferResponse.builder()
                .id(transfer.getId())
                .sourceWarehouseId(transfer.getSourceWarehouse().getId())
                .sourceWarehouseName(transfer.getSourceWarehouse().getName())
                .destinationWarehouseId(transfer.getDestinationWarehouse().getId())
                .destinationWarehouseName(transfer.getDestinationWarehouse().getName())
                .status(transfer.getStatus())
                .requestedBy(transfer.getRequestedBy())
                .approvedBy(transfer.getApprovedBy())
                .createdAt(transfer.getCreatedAt())
                .updatedAt(transfer.getUpdatedAt())
                .items(items)
                .build();
    }

    private WarehouseTransferItemResponse mapToTransferItemResponse(WarehouseTransferItem item) {
        ProductVariant variant = item.getProductVariant();
        return WarehouseTransferItemResponse.builder()
                .id(item.getId())
                .productVariantId(variant.getId())
                .sku(variant.getSku())
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : null)
                .quantity(item.getQuantity())
                .build();
    }
}
