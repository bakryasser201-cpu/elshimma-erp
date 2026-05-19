package com.elshimma.erp.product.dto;

import com.elshimma.erp.product.entity.ProductCategory;
import com.elshimma.erp.product.entity.UnitType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    private String name;
    private String description;
    private ProductCategory category;
    private UnitType unitType;
    private Boolean active;
}
