package com.elshimma.erp.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable audit log for every inventory change.
 *
 * Every time stock changes — whether adding yarn, removing fabric for an order,
 * adjusting after a stocktake, or transferring between warehouses — a new
 * InventoryMovement is created. This record is NEVER updated or deleted.
 *
 * This is the core of the "prevent inventory theft/loss" requirement:
 * managers can trace every single stock change back to who did it, when, and why.
 *
 * Design decisions:
 * - previousQuantity + newQuantity: enables auditors to verify the math
 * - referenceNumber: links to an order number, PO number, or transfer ID
 * - No updatedAt: movements are immutable (append-only log)
 */
@Entity
@Table(name = "inventory_movements", indexes = {
        @Index(name = "idx_movement_inventory", columnList = "inventory_id"),
        @Index(name = "idx_movement_type", columnList = "movement_type"),
        @Index(name = "idx_movement_created_at", columnList = "created_at"),
        @Index(name = "idx_movement_reference", columnList = "reference_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    /**
     * The quantity involved in this movement (always positive).
     * The movementType determines whether it adds or subtracts.
     */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal quantity;

    @Column(name = "previous_quantity", nullable = false, precision = 14, scale = 2)
    private BigDecimal previousQuantity;

    @Column(name = "new_quantity", nullable = false, precision = 14, scale = 2)
    private BigDecimal newQuantity;

    @Column(nullable = false)
    private String reason;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
