package com.elshimma.erp.product.service;

import com.elshimma.erp.product.dto.*;
import com.elshimma.erp.product.entity.Product;
import com.elshimma.erp.product.entity.ProductCategory;
import com.elshimma.erp.product.entity.ProductVariant;
import com.elshimma.erp.product.exception.DuplicateResourceException;
import com.elshimma.erp.product.exception.ResourceNotFoundException;
import com.elshimma.erp.product.repository.ProductRepository;
import com.elshimma.erp.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    // ═══════════════════════════════════════════════════════════════
    //  PRODUCT CRUD
    // ═══════════════════════════════════════════════════════════════

    /**
     * Creates a product with optional variants in a single transaction.
     * If any variant SKU is duplicate, the entire operation rolls back.
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .unitType(request.getUnitType())
                .active(true)
                .build();

        // Add variants if provided
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (CreateVariantRequest variantReq : request.getVariants()) {
                validateSkuUniqueness(variantReq.getSku());
                ProductVariant variant = buildVariantFromRequest(variantReq);
                product.addVariant(variant);
            }
        }

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    /**
     * Retrieves paginated and filtered product list.
     * Supports filtering by keyword (name search), category, and active status.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String keyword,
            ProductCategory category,
            Boolean active,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.findWithFilters(keyword, category, active, pageable);
        return products.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findProductOrThrow(id);
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = findProductOrThrow(id);

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getUnitType() != null) {
            product.setUnitType(request.getUnitType());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    /**
     * Soft-deletes a product by setting active = false.
     * Also deactivates all variants to keep data consistent.
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        product.setActive(false);
        product.getVariants().forEach(v -> v.setActive(false));
        productRepository.save(product);
    }

    // ═══════════════════════════════════════════════════════════════
    //  VARIANT CRUD
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public VariantResponse addVariant(Long productId, CreateVariantRequest request) {
        Product product = findProductOrThrow(productId);
        validateSkuUniqueness(request.getSku());

        ProductVariant variant = buildVariantFromRequest(request);
        product.addVariant(variant);

        productRepository.save(product);

        // Return the last-added variant (the one we just created)
        ProductVariant saved = product.getVariants().get(product.getVariants().size() - 1);
        return mapToVariantResponse(saved);
    }

    @Transactional
    public VariantResponse updateVariant(Long productId, Long variantId, UpdateVariantRequest request) {
        findProductOrThrow(productId);

        ProductVariant variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variant", "id", variantId + " in product " + productId));

        if (request.getSku() != null && !request.getSku().equals(variant.getSku())) {
            validateSkuUniqueness(request.getSku());
            variant.setSku(request.getSku());
        }
        if (request.getColor() != null) {
            variant.setColor(request.getColor());
        }
        if (request.getSize() != null) {
            variant.setSize(request.getSize());
        }
        if (request.getMaterial() != null) {
            variant.setMaterial(request.getMaterial());
        }
        if (request.getSellPrice() != null) {
            variant.setSellPrice(request.getSellPrice());
        }
        if (request.getCostPrice() != null) {
            variant.setCostPrice(request.getCostPrice());
        }
        if (request.getActive() != null) {
            variant.setActive(request.getActive());
        }

        ProductVariant saved = variantRepository.save(variant);
        return mapToVariantResponse(saved);
    }

    @Transactional
    public void deleteVariant(Long productId, Long variantId) {
        findProductOrThrow(productId);
        ProductVariant variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variant", "id", variantId + " in product " + productId));
        variant.setActive(false);
        variantRepository.save(variant);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private void validateSkuUniqueness(String sku) {
        if (variantRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("ProductVariant", "sku", sku);
        }
    }

    private ProductVariant buildVariantFromRequest(CreateVariantRequest request) {
        return ProductVariant.builder()
                .sku(request.getSku())
                .color(request.getColor())
                .size(request.getSize())
                .material(request.getMaterial())
                .sellPrice(request.getSellPrice())
                .costPrice(request.getCostPrice())
                .active(true)
                .build();
    }

    private ProductResponse mapToResponse(Product product) {
        List<VariantResponse> variantResponses = product.getVariants() != null
                ? product.getVariants().stream()
                    .map(this::mapToVariantResponse)
                    .collect(Collectors.toList())
                : Collections.emptyList();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .unitType(product.getUnitType())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .variants(variantResponses)
                .variantCount(variantResponses.size())
                .build();
    }

    private VariantResponse mapToVariantResponse(ProductVariant variant) {
        return VariantResponse.builder()
                .id(variant.getId())
                .sku(variant.getSku())
                .color(variant.getColor())
                .size(variant.getSize())
                .material(variant.getMaterial())
                .sellPrice(variant.getSellPrice())
                .costPrice(variant.getCostPrice())
                .active(variant.isActive())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
}
