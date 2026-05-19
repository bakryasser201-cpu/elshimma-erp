package com.elshimma.erp.product.dto;

import com.elshimma.erp.product.entity.ProductCategory;
import com.elshimma.erp.product.entity.UnitType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private ProductCategory category;
    private UnitType unitType;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<VariantResponse> variants;
    private int variantCount;
}
