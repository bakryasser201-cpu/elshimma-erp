package com.elshimma.erp.warehouse.repository;

import com.elshimma.erp.warehouse.entity.WarehouseTransfer;
import com.elshimma.erp.warehouse.entity.WarehouseTransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseTransferRepository extends JpaRepository<WarehouseTransfer, Long> {
    Page<WarehouseTransfer> findByStatus(WarehouseTransferStatus status, Pageable pageable);
    Page<WarehouseTransfer> findBySourceWarehouseIdOrDestinationWarehouseId(Long sourceWarehouseId, Long destinationWarehouseId, Pageable pageable);
}
