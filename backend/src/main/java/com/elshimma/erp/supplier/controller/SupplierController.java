package com.elshimma.erp.supplier.controller;

import com.elshimma.erp.supplier.dto.*;
import com.elshimma.erp.supplier.entity.PurchaseOrderStatus;
import com.elshimma.erp.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier, procurement, purchase order, and goods receipt APIs")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "Create supplier")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @GetMapping
    @Operation(summary = "List and search suppliers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROCUREMENT_MANAGER', 'FINANCE')")
    public ResponseEntity<Page<SupplierResponse>> getSuppliers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(supplierService.getSuppliers(keyword, active, page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROCUREMENT_MANAGER', 'FINANCE')")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate supplier")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/purchase-orders")
    @Operation(summary = "Create purchase order")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createPurchaseOrder(request));
    }

    @GetMapping("/purchase-orders")
    @Operation(summary = "List purchase orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROCUREMENT_MANAGER', 'FINANCE')")
    public ResponseEntity<Page<PurchaseOrderResponse>> getPurchaseOrders(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(supplierService.getPurchaseOrders(supplierId, status, page, size));
    }

    @PatchMapping("/purchase-orders/{id}/status")
    @Operation(summary = "Update purchase order status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<PurchaseOrderResponse> updatePurchaseOrderStatus(
            @PathVariable Long id,
            @RequestParam PurchaseOrderStatus status) {
        return ResponseEntity.ok(supplierService.updatePurchaseOrderStatus(id, status));
    }

    @PostMapping("/purchase-orders/{id}/receipts")
    @Operation(summary = "Receive purchase order into inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<GoodsReceiptResponse> receivePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody GoodsReceiptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.receivePurchaseOrder(id, request));
    }

    @GetMapping("/{id}/performance")
    @Operation(summary = "Get supplier performance analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROCUREMENT_MANAGER', 'FINANCE')")
    public ResponseEntity<SupplierPerformanceResponse> getSupplierPerformance(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierPerformance(id));
    }
}
