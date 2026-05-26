package com.elshimma.erp.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String referenceNumber;
    private String notes;
    private LocalDateTime createdAt;
}
