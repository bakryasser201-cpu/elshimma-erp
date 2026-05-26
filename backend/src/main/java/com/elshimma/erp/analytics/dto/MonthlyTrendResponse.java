package com.elshimma.erp.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyTrendResponse {
    private String month;
    private BigDecimal revenue;
    private BigDecimal profit;
    private Long orderCount;
}
