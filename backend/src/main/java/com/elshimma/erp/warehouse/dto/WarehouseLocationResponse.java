package com.elshimma.erp.warehouse.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseLocationResponse {
    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private String aisle;
    private String shelf;
    private String binCode;
}
