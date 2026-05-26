package com.elshimma.erp.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInventoryRequest {

    @DecimalMin(value = "0", message = "Minimum quantity cannot be negative")
    private BigDecimal minimumQuantity;

    private Boolean active;
}
