package com.elshimma.erp.finance.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFinancialSummaryResponse {
    private Long orderId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private BigDecimal totalPaid;
    private BigDecimal remainingBalance;
    private BigDecimal calculatedCost;
    private BigDecimal calculatedProfit;
}
