package com.elshimma.erp.analytics.dto;

import java.math.BigDecimal;

public interface ProductPerformanceProjection {
    String getProductName();
    String getVariantSku();
    BigDecimal getTotalSoldQuantity();
    BigDecimal getTotalRevenue();
    BigDecimal getTotalProfit();
}
