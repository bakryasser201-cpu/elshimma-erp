package com.elshimma.erp.hr.repository;

import com.elshimma.erp.hr.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
    boolean existsByTitleIgnoreCase(String title);
}
