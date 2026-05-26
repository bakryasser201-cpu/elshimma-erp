package com.elshimma.erp.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiDashboardResponse {
    private Long totalCompletedOrders;
    private Long pendingOrdersCount;
    private Long activeProductionCount;
    private Long lowStockProductsCount;
    private BigDecimal totalInventoryValue;
}
