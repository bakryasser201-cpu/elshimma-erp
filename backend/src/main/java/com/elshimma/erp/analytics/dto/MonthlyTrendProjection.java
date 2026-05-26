package com.elshimma.erp.analytics.dto;

import java.math.BigDecimal;

public interface MonthlyTrendProjection {
    String getMonth(); // format YYYY-MM
    BigDecimal getRevenue();
    BigDecimal getProfit();
    Long getOrderCount();
}
