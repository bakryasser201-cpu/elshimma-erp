package com.elshimma.erp.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInventoryRequest {

    @NotNull(message = "Product variant ID is required")
    private Long productVariantId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @Builder.Default
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal minimumQuantity = BigDecimal.ZERO;
}
