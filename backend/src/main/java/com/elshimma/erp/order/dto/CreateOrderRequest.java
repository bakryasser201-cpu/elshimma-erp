package com.elshimma.erp.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    private Long customerId;

    private Long clientId;

    private BigDecimal expectedDeposit;

    private LocalDate expectedDeliveryDate;

    private String notes;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<CreateOrderItemRequest> items;

    @AssertTrue(message = "Customer ID is required")
    public boolean isCustomerOrClientProvided() {
        return customerId != null || clientId != null;
    }
}
