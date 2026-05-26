package com.elshimma.erp.inventory.repository;

import com.elshimma.erp.inventory.entity.InventoryMovement;
import com.elshimma.erp.inventory.entity.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    Page<InventoryMovement> findByInventoryId(Long inventoryId, Pageable pageable);

    Page<InventoryMovement> findByMovementType(MovementType type, Pageable pageable);

    /**
     * Full history query with optional filters: inventory, movement type, date range.
     */
    @Query("""
            SELECT m FROM InventoryMovement m
            JOIN m.inventory i
            WHERE (:inventoryId IS NULL OR i.id = :inventoryId)
            AND (:movementType IS NULL OR m.movementType = :movementType)
            AND (:from IS NULL OR m.createdAt >= :from)
            AND (:to IS NULL OR m.createdAt <= :to)
            ORDER BY m.createdAt DESC
            """)
    Page<InventoryMovement> findWithFilters(
            @Param("inventoryId") Long inventoryId,
            @Param("movementType") MovementType movementType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
