package com.elshimma.erp.finance.controller;

import com.elshimma.erp.finance.dto.FinanceDashboardResponse;
import com.elshimma.erp.finance.dto.OrderFinancialSummaryResponse;
import com.elshimma.erp.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinanceDashboardResponse> getDashboard() {
        return ResponseEntity.ok(financeService.getDashboard());
    }

    @GetMapping("/orders/{orderId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderFinancialSummaryResponse> getOrderSummary(@PathVariable Long orderId) {
        return ResponseEntity.ok(financeService.getOrderSummary(orderId));
    }
}
