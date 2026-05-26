package com.elshimma.erp.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseLocationRequest {
    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;
    private String aisle;
    private String shelf;
    @NotBlank(message = "Bin code is required")
    private String binCode;
}
