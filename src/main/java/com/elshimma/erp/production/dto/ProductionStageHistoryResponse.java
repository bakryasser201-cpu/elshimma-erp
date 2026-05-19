package com.elshimma.erp.production.dto;

import com.elshimma.erp.production.entity.ProductionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionStageHistoryResponse {
    private Long id;
    private Long orderId;
    private ProductionStatus productionStatus;
    private String notes;
    private String changedBy;
    private LocalDateTime createdAt;
}
