package com.elshimma.erp.analytics.dto;

import java.math.BigDecimal;

public interface InventoryValuationProjection {
    String getCategory();
    BigDecimal getTotalValue();
}
