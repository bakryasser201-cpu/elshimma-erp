package com.elshimma.erp.customer.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBalanceSummaryResponse {
    private Long customerId;
    private String companyName;
    private BigDecimal creditLimit;
    private BigDecimal totalOrders;
    private BigDecimal totalPaid;
    private BigDecimal outstandingBalance;
    private BigDecimal availableCredit;
}
