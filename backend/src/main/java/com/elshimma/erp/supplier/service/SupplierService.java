package com.elshimma.erp.supplier.service;

import com.elshimma.erp.inventory.dto.StockMovementRequest;
import com.elshimma.erp.inventory.entity.Warehouse;
import com.elshimma.erp.inventory.repository.WarehouseRepository;
import com.elshimma.erp.inventory.service.InventoryService;
import com.elshimma.erp.product.entity.ProductVariant;
import com.elshimma.erp.product.exception.DuplicateResourceException;
import com.elshimma.erp.product.exception.ResourceNotFoundException;
import com.elshimma.erp.product.repository.ProductVariantRepository;
import com.elshimma.erp.supplier.dto.*;
import com.elshimma.erp.supplier.entity.*;
import com.elshimma.erp.supplier.exception.InvalidPurchaseOrderStateException;
import com.elshimma.erp.supplier.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptNoteRepository goodsReceiptNoteRepository;
    private final ProductVariantRepository variantRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        validateSupplierEmail(request.getEmail());
        Supplier supplier = Supplier.builder()
                .companyName(request.getCompanyName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .paymentTerms(request.getPaymentTerms())
                .rating(request.getRating())
                .active(true)
                .build();
        return mapToSupplierResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = findSupplierOrThrow(id);
        if (request.getCompanyName() != null) supplier.setCompanyName(request.getCompanyName());
        if (request.getContactPerson() != null) supplier.setContactPerson(request.getContactPerson());
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(supplier.getEmail())) {
            validateSupplierEmail(request.getEmail());
            supplier.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) supplier.setPhone(request.getPhone());
        if (request.getAddress() != null) supplier.setAddress(request.getAddress());
        if (request.getPaymentTerms() != null) supplier.setPaymentTerms(request.getPaymentTerms());
        if (request.getRating() != null) supplier.setRating(request.getRating());
        if (request.getActive() != null) supplier.setActive(request.getActive());
        return mapToSupplierResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = findSupplierOrThrow(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        return mapToSupplierResponse(findSupplierOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> getSuppliers(String keyword, Boolean active, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return supplierRepository.findWithFilters(normalizedKeyword, active, PageRequest.of(page, size, sort))
                .map(this::mapToSupplierResponse);
    }

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        Supplier supplier = findSupplierOrThrow(request.getSupplierId());
        if (!supplier.isActive()) {
            throw new InvalidPurchaseOrderStateException("Cannot create purchase order for inactive supplier " + supplier.getId());
        }

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(supplier)
                .status(PurchaseOrderStatus.DRAFT)
                .notes(request.getNotes())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemRequest.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", itemRequest.getProductVariantId()));
            BigDecimal subtotal = itemRequest.getQuantity().multiply(itemRequest.getUnitPrice());
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(purchaseOrder)
                    .productVariant(variant)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .subtotal(subtotal)
                    .build();
            purchaseOrder.getItems().add(item);
            totalAmount = totalAmount.add(subtotal);
        }
        purchaseOrder.setTotalAmount(totalAmount);
        return mapToPurchaseOrderResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> getPurchaseOrders(Long supplierId, PurchaseOrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (supplierId != null) {
            return purchaseOrderRepository.findBySupplierId(supplierId, pageable).map(this::mapToPurchaseOrderResponse);
        }
        if (status != null) {
            return purchaseOrderRepository.findByStatus(status, pageable).map(this::mapToPurchaseOrderResponse);
        }
        return purchaseOrderRepository.findAll(pageable).map(this::mapToPurchaseOrderResponse);
    }

    @Transactional
    public PurchaseOrderResponse updatePurchaseOrderStatus(Long id, PurchaseOrderStatus status) {
        PurchaseOrder purchaseOrder = findPurchaseOrderOrThrow(id);
        validatePurchaseOrderTransition(purchaseOrder.getStatus(), status);
        purchaseOrder.setStatus(status);
        return mapToPurchaseOrderResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public GoodsReceiptResponse receivePurchaseOrder(Long purchaseOrderId, GoodsReceiptRequest request) {
        PurchaseOrder purchaseOrder = findPurchaseOrderOrThrow(purchaseOrderId);
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.CONFIRMED && purchaseOrder.getStatus() != PurchaseOrderStatus.SENT) {
            throw new InvalidPurchaseOrderStateException("Only SENT or CONFIRMED purchase orders can be received");
        }
        if (goodsReceiptNoteRepository.existsByPurchaseOrderId(purchaseOrderId)) {
            throw new DuplicateResourceException("GoodsReceiptNote", "purchaseOrderId", purchaseOrderId);
        }

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", request.getWarehouseId()));

        GoodsReceiptNote grn = GoodsReceiptNote.builder()
                .purchaseOrder(purchaseOrder)
                .warehouse(warehouse)
                .receivedBy(request.getReceivedBy())
                .notes(request.getNotes())
                .build();
        GoodsReceiptNote saved = goodsReceiptNoteRepository.save(grn);

        for (PurchaseOrderItem item : purchaseOrder.getItems()) {
            inventoryService.receiveStock(item.getProductVariant().getId(), warehouse.getId(), StockMovementRequest.builder()
                    .quantity(item.getQuantity())
                    .reason("Goods receipt")
                    .referenceNumber("PO-" + purchaseOrder.getId())
                    .notes("Received against purchase order " + purchaseOrder.getId())
                    .build());
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        purchaseOrderRepository.save(purchaseOrder);

        return mapToGoodsReceiptResponse(saved);
    }

    @Transactional(readOnly = true)
    public SupplierPerformanceResponse getSupplierPerformance(Long supplierId) {
        Supplier supplier = findSupplierOrThrow(supplierId);
        return SupplierPerformanceResponse.builder()
                .supplierId(supplier.getId())
                .supplierName(supplier.getCompanyName())
                .rating(supplier.getRating())
                .purchaseOrderCount(purchaseOrderRepository.countBySupplierId(supplierId))
                .totalPurchased(purchaseOrderRepository.sumTotalAmountBySupplierId(supplierId))
                .receivedOrderCount(purchaseOrderRepository.countBySupplierIdAndStatus(supplierId, PurchaseOrderStatus.RECEIVED))
                .build();
    }

    private Supplier findSupplierOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }

    private PurchaseOrder findPurchaseOrderOrThrow(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
    }

    private void validateSupplierEmail(String email) {
        if (email != null && !email.isBlank() && supplierRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Supplier", "email", email);
        }
    }

    private void validatePurchaseOrderTransition(PurchaseOrderStatus current, PurchaseOrderStatus target) {
        boolean valid = current == target
                || current == PurchaseOrderStatus.DRAFT && target == PurchaseOrderStatus.SENT
                || current == PurchaseOrderStatus.SENT && target == PurchaseOrderStatus.CONFIRMED
                || current == PurchaseOrderStatus.RECEIVED && target == PurchaseOrderStatus.CLOSED;
        if (!valid) {
            throw new InvalidPurchaseOrderStateException("Invalid purchase order transition from " + current + " to " + target);
        }
    }

    private SupplierResponse mapToSupplierResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .companyName(supplier.getCompanyName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .paymentTerms(supplier.getPaymentTerms())
                .rating(supplier.getRating())
                .active(supplier.isActive())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }

    private PurchaseOrderResponse mapToPurchaseOrderResponse(PurchaseOrder purchaseOrder) {
        List<PurchaseOrderItemResponse> items = purchaseOrder.getItems().stream()
                .map(this::mapToPurchaseOrderItemResponse)
                .collect(Collectors.toList());
        return PurchaseOrderResponse.builder()
                .id(purchaseOrder.getId())
                .supplierId(purchaseOrder.getSupplier().getId())
                .supplierName(purchaseOrder.getSupplier().getCompanyName())
                .status(purchaseOrder.getStatus())
                .totalAmount(purchaseOrder.getTotalAmount())
                .notes(purchaseOrder.getNotes())
                .createdAt(purchaseOrder.getCreatedAt())
                .updatedAt(purchaseOrder.getUpdatedAt())
                .items(items)
                .build();
    }

    private PurchaseOrderItemResponse mapToPurchaseOrderItemResponse(PurchaseOrderItem item) {
        ProductVariant variant = item.getProductVariant();
        return PurchaseOrderItemResponse.builder()
                .id(item.getId())
                .productVariantId(variant.getId())
                .sku(variant.getSku())
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    private GoodsReceiptResponse mapToGoodsReceiptResponse(GoodsReceiptNote grn) {
        return GoodsReceiptResponse.builder()
                .id(grn.getId())
                .purchaseOrderId(grn.getPurchaseOrder().getId())
                .warehouseId(grn.getWarehouse().getId())
                .warehouseName(grn.getWarehouse().getName())
                .receivedAt(grn.getReceivedAt())
                .receivedBy(grn.getReceivedBy())
                .notes(grn.getNotes())
                .build();
    }
}
