package com.elshimma.erp.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPerformanceResponse {
    private String productName;
    private String variantSku;
    private BigDecimal totalSoldQuantity;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
}
