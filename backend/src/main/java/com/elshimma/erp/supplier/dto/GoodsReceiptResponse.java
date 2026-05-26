package com.elshimma.erp.supplier.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptResponse {
    private Long id;
    private Long purchaseOrderId;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime receivedAt;
    private String receivedBy;
    private String notes;
}
