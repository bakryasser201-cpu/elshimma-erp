package com.elshimma.erp.analytics.service;

import com.elshimma.erp.analytics.dto.*;
import com.elshimma.erp.inventory.entity.Inventory;
import com.elshimma.erp.inventory.repository.InventoryRepository;
import com.elshimma.erp.order.entity.OrderStatus;
import com.elshimma.erp.order.repository.CustomerOrderRepository;
import com.elshimma.erp.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public KpiDashboardResponse getKpiDashboard() {
        return KpiDashboardResponse.builder()
                .totalCompletedOrders(orderRepository.countByOrderStatusAndActiveTrue(OrderStatus.DELIVERED))
                .pendingOrdersCount(orderRepository.countByOrderStatusAndActiveTrue(OrderStatus.PENDING))
                .activeProductionCount(orderRepository.countByOrderStatusAndActiveTrue(OrderStatus.IN_PRODUCTION))
                .lowStockProductsCount(inventoryRepository.countByActiveTrueAndCurrentQuantityLessThanEqual(BigDecimal.ZERO)) // threshold could be parametrized
                .totalInventoryValue(inventoryRepository.sumTotalInventoryValue())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MonthlyTrendProjection> getMonthlySalesTrends(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(12);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        return orderRepository.getMonthlySalesTrends(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<ProductPerformanceProjection> getTopSellingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderItemRepository.getTopSellingProducts(pageable);
    }

    @Transactional(readOnly = true)
    public List<InventoryValuationProjection> getInventoryValuation() {
        return inventoryRepository.getInventoryValuationByCategory();
    }

    @Transactional(readOnly = true)
    public Page<DeadStockResponse> getDeadStock(int monthsInactive, int page, int size) {
        LocalDateTime thresholdDate = LocalDateTime.now().minusMonths(monthsInactive);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Inventory> deadStockPage = inventoryRepository.findDeadStock(thresholdDate, pageable);
        
        return deadStockPage.map(inv -> DeadStockResponse.builder()
                .inventoryId(inv.getId())
                .variantSku(inv.getProductVariant().getSku())
                .currentQuantity(inv.getCurrentQuantity())
                .lastMovementDate(inv.getUpdatedAt())
                .location(inv.getWarehouse().getName())
                .build());
    }
}
