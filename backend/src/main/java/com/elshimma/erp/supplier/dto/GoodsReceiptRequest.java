package com.elshimma.erp.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptRequest {
    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;
    @NotBlank(message = "Received by is required")
    private String receivedBy;
    private String notes;
}
