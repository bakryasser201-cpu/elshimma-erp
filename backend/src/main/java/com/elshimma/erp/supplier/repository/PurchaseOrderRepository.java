package com.elshimma.erp.supplier.repository;

import com.elshimma.erp.supplier.entity.PurchaseOrder;
import com.elshimma.erp.supplier.entity.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Page<PurchaseOrder> findBySupplierId(Long supplierId, Pageable pageable);
    Page<PurchaseOrder> findByStatus(PurchaseOrderStatus status, Pageable pageable);
    long countBySupplierId(Long supplierId);
    long countBySupplierIdAndStatus(Long supplierId, PurchaseOrderStatus status);

    @Query("SELECT COALESCE(SUM(po.totalAmount), 0) FROM PurchaseOrder po WHERE po.supplier.id = :supplierId")
    BigDecimal sumTotalAmountBySupplierId(@Param("supplierId") Long supplierId);
}
