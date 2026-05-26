package com.elshimma.erp.warehouse.repository;

import com.elshimma.erp.warehouse.entity.WarehouseLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, Long> {
    Page<WarehouseLocation> findByWarehouseId(Long warehouseId, Pageable pageable);
    boolean existsByWarehouseIdAndBinCodeIgnoreCase(Long warehouseId, String binCode);
}
