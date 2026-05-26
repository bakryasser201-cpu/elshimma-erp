package com.elshimma.erp.analytics.controller;

import com.elshimma.erp.analytics.dto.*;
import com.elshimma.erp.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/kpi")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<KpiDashboardResponse> getKpiDashboard() {
        return ResponseEntity.ok(analyticsService.getKpiDashboard());
    }

    @GetMapping("/sales/trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<MonthlyTrendProjection>> getMonthlySalesTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(analyticsService.getMonthlySalesTrends(startDate, endDate));
    }

    @GetMapping("/sales/top-products")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ProductPerformanceProjection>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopSellingProducts(limit));
    }

    @GetMapping("/inventory/valuation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<InventoryValuationProjection>> getInventoryValuation() {
        return ResponseEntity.ok(analyticsService.getInventoryValuation());
    }

    @GetMapping("/inventory/dead-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<DeadStockResponse>> getDeadStock(
            @RequestParam(defaultValue = "6") int monthsInactive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(analyticsService.getDeadStock(monthsInactive, page, size));
    }
}
