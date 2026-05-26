package com.elshimma.erp.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A specific sellable/trackable variant of a Product.
 *
 * Example: Product "School Gabardine" might have variants:
 *   - SKU: FAB-SCH-BLU-150 → Blue, 150cm wide, 25.00 EGP/m
 *   - SKU: FAB-SCH-WHT-100 → White, 100cm wide, 20.00 EGP/m
 *
 * Design decisions:
 * - SKU is unique across the entire system — prevents duplicate stock entries
 * - BigDecimal for sellPrice — never use float/double for money
 * - color/size/material are nullable because not every product type needs all three
 *   (e.g. a badge might only have color, not size)
 * - Soft-delete via 'active' flag
 */
@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_variant_sku", columnList = "sku", unique = true),
        @Index(name = "idx_variant_product_id", columnList = "product_id"),
        @Index(name = "idx_variant_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    private String color;

    private String size;

    private String material;

    @Column(name = "sell_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellPrice;

    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
