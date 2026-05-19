package com.elshimma.erp.production.dto;

import com.elshimma.erp.production.entity.ProductionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductionStatusRequest {
    @NotNull(message = "Production status is required")
    private ProductionStatus productionStatus;

    private String notes;
}
