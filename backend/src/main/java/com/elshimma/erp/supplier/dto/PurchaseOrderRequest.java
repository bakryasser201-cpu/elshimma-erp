package com.elshimma.erp.supplier.dto;

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
public class PurchaseOrderRequest {
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;
    private String notes;
    @NotEmpty(message = "Purchase order must have at least one item")
    @Valid
    private List<PurchaseOrderItemRequest> items;
}
