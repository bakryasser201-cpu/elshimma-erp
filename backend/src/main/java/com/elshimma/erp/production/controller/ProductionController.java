package com.elshimma.erp.production.controller;

import com.elshimma.erp.production.dto.ProductionStageHistoryResponse;
import com.elshimma.erp.production.dto.UpdateProductionStatusRequest;
import com.elshimma.erp.production.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductionStageHistoryResponse> updateProductionStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateProductionStatusRequest request) {
        return ResponseEntity.ok(productionService.updateProductionStatus(orderId, request));
    }

    @GetMapping("/{orderId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ProductionStageHistoryResponse>> getProductionHistory(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(productionService.getProductionHistory(orderId));
    }
}
