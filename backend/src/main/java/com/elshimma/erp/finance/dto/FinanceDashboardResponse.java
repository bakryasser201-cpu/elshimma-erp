package com.elshimma.erp.finance.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceDashboardResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private BigDecimal totalPaid;
    private BigDecimal totalUnpaid;
}
