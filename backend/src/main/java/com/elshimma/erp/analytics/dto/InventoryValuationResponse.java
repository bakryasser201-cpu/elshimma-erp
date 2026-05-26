package com.elshimma.erp.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryValuationResponse {
    private String category; // or variant Sku if per product
    private BigDecimal totalValue;
}
