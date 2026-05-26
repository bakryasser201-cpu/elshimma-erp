package com.elshimma.erp.analytics.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadStockResponse {
    private Long inventoryId;
    private String variantSku;
    private BigDecimal currentQuantity;
    private LocalDateTime lastMovementDate;
    private String location;
}
