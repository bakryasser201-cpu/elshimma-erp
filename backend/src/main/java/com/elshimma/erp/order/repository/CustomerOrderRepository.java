package com.elshimma.erp.order.repository;

import com.elshimma.erp.order.entity.CustomerOrder;
import com.elshimma.erp.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    Page<CustomerOrder> findByActiveTrue(Pageable pageable);

    Page<CustomerOrder> findByClientIdAndActiveTrue(Long clientId, Pageable pageable);

    Page<CustomerOrder> findByCustomerIdAndActiveTrue(Long customerId, Pageable pageable);

    Page<CustomerOrder> findByOrderStatusAndActiveTrue(OrderStatus orderStatus, Pageable pageable);

    @Query("""
            SELECT o FROM CustomerOrder o
            WHERE o.active = true
            AND o.expectedDeliveryDate IS NOT NULL
            AND o.actualDeliveryDate IS NULL
            AND o.expectedDeliveryDate < :currentDate
            """)
    Page<CustomerOrder> findDelayedOrders(@Param("currentDate") LocalDate currentDate, Pageable pageable);
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM CustomerOrder o WHERE o.active = true AND o.orderStatus != 'CANCELLED'")
    java.math.BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.estimatedProfit), 0) FROM CustomerOrder o WHERE o.active = true AND o.orderStatus != 'CANCELLED'")
    java.math.BigDecimal sumTotalProfit();

    @Query("SELECT COALESCE(SUM(o.depositAmount), 0) FROM CustomerOrder o WHERE o.active = true AND o.orderStatus != 'CANCELLED'")
    java.math.BigDecimal sumTotalPaid();

    @Query("SELECT COALESCE(SUM(o.remainingAmount), 0) FROM CustomerOrder o WHERE o.active = true AND o.orderStatus != 'CANCELLED'")
    java.math.BigDecimal sumTotalUnpaid();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM CustomerOrder o WHERE o.customer.id = :customerId AND o.active = true AND o.orderStatus != 'CANCELLED'")
    java.math.BigDecimal sumTotalAmountByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(o.depositAmount), 0) FROM CustomerOrder o WHERE o.customer.id = :customerId AND o.active = true AND o.orderStatus != 'CANCELLED'")
    java.math.BigDecimal sumPaidAmountByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(o.remainingAmount), 0) FROM CustomerOrder o WHERE o.customer.id = :customerId AND o.active = true AND o.orderStatus != 'CANCELLED'")
    java.math.BigDecimal sumOutstandingBalanceByCustomerId(@Param("customerId") Long customerId);

    long countByOrderStatusAndActiveTrue(OrderStatus status);

    @Query(value = """
        SELECT TO_CHAR(o.created_at, 'YYYY-MM') AS month,
               COALESCE(SUM(o.total_amount), 0) AS revenue,
               COALESCE(SUM(o.estimated_profit), 0) AS profit,
               COUNT(o.id) AS orderCount
        FROM customer_orders o
        WHERE o.active = true AND o.order_status != 'CANCELLED'
          AND o.created_at >= :startDate AND o.created_at <= :endDate
        GROUP BY TO_CHAR(o.created_at, 'YYYY-MM')
        ORDER BY month ASC
        """, nativeQuery = true)
    java.util.List<com.elshimma.erp.analytics.dto.MonthlyTrendProjection> getMonthlySalesTrends(
            @Param("startDate") java.time.LocalDateTime startDate, 
            @Param("endDate") java.time.LocalDateTime endDate);
}
