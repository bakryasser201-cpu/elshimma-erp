package com.elshimma.erp.order.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productVariantId;
    private String variantSku;
    
    private String productNameSnapshot;
    private String variantNameSnapshot;

    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal unitCostAtOrderTime;
    private BigDecimal totalPrice;
    private BigDecimal reservedQuantity;

    private String notes;
}
