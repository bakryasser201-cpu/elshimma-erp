package com.elshimma.erp.warehouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransferRequest {
    @NotNull(message = "Source warehouse ID is required")
    private Long sourceWarehouseId;
    @NotNull(message = "Destination warehouse ID is required")
    private Long destinationWarehouseId;
    @NotNull(message = "Requested by is required")
    private String requestedBy;
    @NotEmpty(message = "Transfer must have at least one item")
    @Valid
    private List<WarehouseTransferItemRequest> items;
}
