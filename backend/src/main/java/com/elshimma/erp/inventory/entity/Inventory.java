package com.elshimma.erp.inventory.entity;

import com.elshimma.erp.product.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks the stock level of one ProductVariant in one Warehouse.
 *
 * Design decisions:
 * - UniqueConstraint on (product_variant_id, warehouse_id): one row per variant per warehouse
 * - currentQuantity: available stock (what can be sold/used right now)
 * - reservedQuantity: stock locked for pending orders (cannot be sold to others)
 * - minimumQuantity: low-stock threshold — triggers alerts when currentQuantity drops below this
 * - BigDecimal for quantities because fabric is tracked in meters (e.g. 150.75m)
 * - Soft-delete via active flag
 */
@Entity
@Table(name = "inventories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_variant_warehouse",
                        columnNames = {"product_variant_id", "warehouse_id"}
                )
        },
        indexes = {
                @Index(name = "idx_inventory_variant", columnList = "product_variant_id"),
                @Index(name = "idx_inventory_warehouse", columnList = "warehouse_id"),
                @Index(name = "idx_inventory_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Builder.Default
    @Column(name = "current_quantity", nullable = false, precision = 14, scale = 2)
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "reserved_quantity", nullable = false, precision = 14, scale = 2)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "minimum_quantity", nullable = false, precision = 14, scale = 2)
    private BigDecimal minimumQuantity = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Computed helpers ─────────────────────────────────────────

    /**
     * Available = current - reserved.
     * This is what can actually be sold or used.
     */
    public BigDecimal getAvailableQuantity() {
        return currentQuantity.subtract(reservedQuantity);
    }

    /**
     * Returns true if current stock is at or below the minimum threshold.
     */
    public boolean isLowStock() {
        return currentQuantity.compareTo(minimumQuantity) <= 0;
    }
}
