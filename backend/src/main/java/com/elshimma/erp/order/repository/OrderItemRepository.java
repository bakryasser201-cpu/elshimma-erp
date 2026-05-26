package com.elshimma.erp.order.repository;

import com.elshimma.erp.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        SELECT i.productNameSnapshot AS productName,
               i.variantNameSnapshot AS variantSku,
               SUM(i.quantity) AS totalSoldQuantity,
               SUM(i.totalPrice) AS totalRevenue,
               SUM(i.totalPrice - (i.unitCostAtOrderTime * i.quantity)) AS totalProfit
        FROM OrderItem i
        WHERE i.order.active = true AND i.order.orderStatus != 'CANCELLED'
        GROUP BY i.productNameSnapshot, i.variantNameSnapshot
        ORDER BY totalRevenue DESC
        """)
    java.util.List<com.elshimma.erp.analytics.dto.ProductPerformanceProjection> getTopSellingProducts(Pageable pageable);
}
