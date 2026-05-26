package com.elshimma.erp.warehouse.dto;

import com.elshimma.erp.warehouse.entity.WarehouseTransferStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransferResponse {
    private Long id;
    private Long sourceWarehouseId;
    private String sourceWarehouseName;
    private Long destinationWarehouseId;
    private String destinationWarehouseName;
    private WarehouseTransferStatus status;
    private String requestedBy;
    private String approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<WarehouseTransferItemResponse> items;
}
