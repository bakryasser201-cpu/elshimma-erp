package com.elshimma.erp.product.dto;

import com.elshimma.erp.product.entity.ProductCategory;
import com.elshimma.erp.product.entity.UnitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private ProductCategory category;

    @NotNull(message = "Unit type is required")
    private UnitType unitType;

    /**
     * Optional list of variants to create along with the product.
     * If empty, the product is created without any variants.
     */
    @Valid
    private List<CreateVariantRequest> variants;
}
