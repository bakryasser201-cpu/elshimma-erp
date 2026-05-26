package com.elshimma.erp.order.dto;

import com.elshimma.erp.order.entity.OrderStatus;
import com.elshimma.erp.production.entity.ProductionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerCompanyName;
    private Long clientId;
    private String companyName;
    
    private OrderStatus orderStatus;
    private ProductionStatus productionStatus;
    
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal remainingAmount;
    
    private BigDecimal estimatedCost;
    private BigDecimal estimatedProfit;

    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    
    private LocalDateTime productionStartedAt;
    private LocalDateTime readyAt;
    private LocalDateTime deliveredAt;

    private String notes;
    private boolean active;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OrderItemResponse> items;
    private List<OrderPaymentResponse> payments;
}
