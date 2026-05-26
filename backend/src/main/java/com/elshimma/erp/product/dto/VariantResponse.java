package com.elshimma.erp.product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantResponse {

    private Long id;
    private String sku;
    private String color;
    private String size;
    private String material;
    private BigDecimal sellPrice;
    private BigDecimal costPrice;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
