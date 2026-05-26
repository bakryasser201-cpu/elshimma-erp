package com.elshimma.erp.inventory.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private Long id;

    // Variant info (flattened for convenience)
    private Long productVariantId;
    private String variantSku;
    private String productName;
    private String color;
    private String size;

    // Warehouse info
    private Long warehouseId;
    private String warehouseName;

    // Quantities
    private BigDecimal currentQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal minimumQuantity;

    private boolean lowStock;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
