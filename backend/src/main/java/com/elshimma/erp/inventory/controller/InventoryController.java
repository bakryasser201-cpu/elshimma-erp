package com.elshimma.erp.inventory.controller;

import com.elshimma.erp.inventory.dto.*;
import com.elshimma.erp.inventory.entity.MovementType;
import com.elshimma.erp.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ═══════════════════════════════════════════════════════════════
    //  INVENTORY CRUD
    // ═══════════════════════════════════════════════════════════════

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody CreateInventoryRequest request) {
        InventoryResponse response = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<InventoryResponse>> getInventories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<InventoryResponse> response = inventoryService.getInventories(
                keyword, warehouseId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, request));
    }

    // ═══════════════════════════════════════════════════════════════
    //  STOCK OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/{id}/add-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(inventoryService.addStock(id, request));
    }

    @PostMapping("/{id}/remove-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryResponse> removeStock(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(inventoryService.removeStock(id, request));
    }

    @PostMapping("/{id}/reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryResponse> reserveStock(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(inventoryService.reserveStock(id, request));
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryResponse> releaseStock(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(inventoryService.releaseStock(id, request));
    }

    // ═══════════════════════════════════════════════════════════════
    //  QUERIES
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<InventoryResponse>> getLowStockItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(inventoryService.getLowStockItems(page, size));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<InventoryMovementResponse>> getMovementHistory(
            @RequestParam(required = false) Long inventoryId,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<InventoryMovementResponse> response = inventoryService.getMovementHistory(
                inventoryId, movementType, from, to, page, size);
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════════
    //  WAREHOUSE
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/warehouses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createWarehouse(request));
    }

    @GetMapping("/warehouses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<WarehouseResponse>> getWarehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(inventoryService.getWarehouses(page, size));
    }
}
