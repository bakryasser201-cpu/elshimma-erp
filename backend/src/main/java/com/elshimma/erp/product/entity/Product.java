package com.elshimma.erp.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product in the El Shimma catalog.
 *
 * A Product is the "parent" concept — e.g. "School Gabardine Fabric".
 * Each product can have multiple ProductVariant records for specific
 * color/size/material combinations (e.g. Blue 150cm, White 100cm).
 *
 * Design decisions:
 * - category and unitType are enums (fixed business concepts, rarely change)
 * - Soft-delete via 'active' flag — never lose historical data
 * - CascadeType.ALL on variants so creating a product with variants is atomic
 * - orphanRemoval ensures removed variants are deleted from DB
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category", columnList = "category"),
        @Index(name = "idx_product_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false)
    private UnitType unitType;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * One product can have many variants.
     * CascadeType.ALL: when we save a product, its variants are saved too.
     * orphanRemoval: if a variant is removed from this list, it's deleted from DB.
     */
    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    // ── Helper methods ──────────────────────────────────────────────

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }
}
