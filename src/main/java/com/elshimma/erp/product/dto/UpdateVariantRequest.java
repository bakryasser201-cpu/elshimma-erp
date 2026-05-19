package com.elshimma.erp.product.dto;

import lombok.*;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVariantRequest {

    private String sku;
    private String color;
    private String size;
    private String material;

    @DecimalMin(value = "0.01", message = "Sell price must be greater than zero")
    private BigDecimal sellPrice;

    @DecimalMin(value = "0.00", message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    private Boolean active;
}
