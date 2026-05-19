package com.elshimma.erp.inventory.repository;

import com.elshimma.erp.inventory.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Page<Warehouse> findByActiveTrue(Pageable pageable);

    Optional<Warehouse> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
