package com.elshimma.erp.supplier.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierPerformanceResponse {
    private Long supplierId;
    private String supplierName;
    private BigDecimal rating;
    private long purchaseOrderCount;
    private BigDecimal totalPurchased;
    private long receivedOrderCount;
}
