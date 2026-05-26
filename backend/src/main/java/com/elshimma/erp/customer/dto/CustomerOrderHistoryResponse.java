package com.elshimma.erp.customer.dto;

import com.elshimma.erp.order.entity.OrderStatus;
import com.elshimma.erp.production.entity.ProductionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderHistoryResponse {
    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private ProductionStatus productionStatus;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private LocalDateTime createdAt;
}
