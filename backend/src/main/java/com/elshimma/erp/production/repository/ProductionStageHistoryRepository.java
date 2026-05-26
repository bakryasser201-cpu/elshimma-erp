package com.elshimma.erp.production.repository;

import com.elshimma.erp.production.entity.ProductionStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductionStageHistoryRepository extends JpaRepository<ProductionStageHistory, Long> {
    List<ProductionStageHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
