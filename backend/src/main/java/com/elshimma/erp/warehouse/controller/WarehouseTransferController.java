package com.elshimma.erp.warehouse.controller;

import com.elshimma.erp.warehouse.dto.*;
import com.elshimma.erp.warehouse.entity.WarehouseTransferStatus;
import com.elshimma.erp.warehouse.service.WarehouseTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
@Tag(name = "Advanced Warehouse", description = "Warehouse locations and transfer workflow APIs")
public class WarehouseTransferController {

    private final WarehouseTransferService warehouseTransferService;

    @PostMapping("/locations")
    @Operation(summary = "Create warehouse location")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<WarehouseLocationResponse> createLocation(@Valid @RequestBody WarehouseLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseTransferService.createLocation(request));
    }

    @GetMapping("/locations")
    @Operation(summary = "List warehouse locations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<WarehouseLocationResponse>> getLocations(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(warehouseTransferService.getLocations(warehouseId, page, size));
    }

    @PostMapping("/transfers")
    @Operation(summary = "Create warehouse transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<WarehouseTransferResponse> createTransfer(@Valid @RequestBody WarehouseTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseTransferService.createTransfer(request));
    }

    @GetMapping("/transfers")
    @Operation(summary = "List warehouse transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<WarehouseTransferResponse>> getTransfers(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) WarehouseTransferStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(warehouseTransferService.getTransfers(warehouseId, status, page, size));
    }

    @PatchMapping("/transfers/{id}/approve")
    @Operation(summary = "Approve warehouse transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<WarehouseTransferResponse> approveTransfer(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(warehouseTransferService.approveTransfer(id, approvedBy));
    }

    @PatchMapping("/transfers/{id}/complete")
    @Operation(summary = "Complete warehouse transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<WarehouseTransferResponse> completeTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseTransferService.completeTransfer(id));
    }

    @PatchMapping("/transfers/{id}/cancel")
    @Operation(summary = "Cancel warehouse transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<WarehouseTransferResponse> cancelTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseTransferService.cancelTransfer(id));
    }
}
