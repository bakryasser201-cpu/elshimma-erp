package com.elshimma.erp.warehouse.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransferItemResponse {
    private Long id;
    private Long productVariantId;
    private String sku;
    private String productName;
    private BigDecimal quantity;
}
