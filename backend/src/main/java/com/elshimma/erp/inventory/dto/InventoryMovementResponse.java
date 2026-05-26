package com.elshimma.erp.inventory.dto;

import com.elshimma.erp.inventory.entity.MovementType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementResponse {

    private Long id;
    private Long inventoryId;
    private MovementType movementType;
    private BigDecimal quantity;
    private BigDecimal previousQuantity;
    private BigDecimal newQuantity;
    private String reason;
    private String referenceNumber;
    private String notes;
    private LocalDateTime createdAt;

    // Contextual info
    private String variantSku;
    private String productName;
    private String warehouseName;
}
