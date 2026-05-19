package com.elshimma.erp.inventory.repository;

import com.elshimma.erp.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductVariantIdAndWarehouseId(Long variantId, Long warehouseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.id = :id")
    Optional<Inventory> findByIdForUpdate(@Param("id") Long id);

    boolean existsByProductVariantIdAndWarehouseId(Long variantId, Long warehouseId);

    Page<Inventory> findByActiveTrue(Pageable pageable);

    Page<Inventory> findByWarehouseIdAndActiveTrue(Long warehouseId, Pageable pageable);

    List<Inventory> findByProductVariantIdAndActiveTrue(Long variantId);

    /**
     * Returns inventory records where currentQuantity is at or below minimumQuantity.
     * This powers the low-stock alerts dashboard.
     */
    @Query("""
            SELECT i FROM Inventory i
            WHERE i.currentQuantity <= i.minimumQuantity
            AND i.active = true
            """)
    Page<Inventory> findLowStockItems(Pageable pageable);

    /**
     * Combined filter query supporting keyword search (by variant SKU or product name),
     * warehouse filter, and active-only filter.
     */
    @Query(
            value = """
                    SELECT i FROM Inventory i
                    JOIN FETCH i.productVariant pv
                    JOIN FETCH pv.product p
                    JOIN FETCH i.warehouse w
                    WHERE i.active = true
                    AND (:warehouseId IS NULL OR w.id = :warehouseId)
                    AND (:keyword IS NULL
                         OR LOWER(pv.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                         OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                    """,
            countQuery = """
                    SELECT COUNT(i) FROM Inventory i
                    JOIN i.productVariant pv
                    JOIN pv.product p
                    JOIN i.warehouse w
                    WHERE i.active = true
                    AND (:warehouseId IS NULL OR w.id = :warehouseId)
                    AND (:keyword IS NULL
                         OR LOWER(pv.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                         OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                    """
    )
    Page<Inventory> findWithFilters(
            @Param("keyword") String keyword,
            @Param("warehouseId") Long warehouseId,
            Pageable pageable
    );
}
