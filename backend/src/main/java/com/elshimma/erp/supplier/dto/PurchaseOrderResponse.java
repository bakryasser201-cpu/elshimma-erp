package com.elshimma.erp.supplier.dto;

import com.elshimma.erp.supplier.entity.PurchaseOrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private PurchaseOrderStatus status;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PurchaseOrderItemResponse> items;
}
