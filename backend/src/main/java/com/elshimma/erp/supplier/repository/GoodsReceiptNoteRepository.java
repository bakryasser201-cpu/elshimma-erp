package com.elshimma.erp.supplier.repository;

import com.elshimma.erp.supplier.entity.GoodsReceiptNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsReceiptNoteRepository extends JpaRepository<GoodsReceiptNote, Long> {
    boolean existsByPurchaseOrderId(Long purchaseOrderId);
}
