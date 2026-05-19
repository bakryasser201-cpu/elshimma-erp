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

    Page<CustomerOrder> findByOrderStatusAndActiveTrue(OrderStatus orderStatus, Pageable pageable);

    @Query("""
            SELECT o FROM CustomerOrder o
            WHERE o.active = true
            AND o.expectedDeliveryDate IS NOT NULL
            AND o.actualDeliveryDate IS NULL
            AND o.expectedDeliveryDate < :currentDate
            """)
    Page<CustomerOrder> findDelayedOrders(@Param("currentDate") LocalDate currentDate, Pageable pageable);

}
