package com.elshimma.erp.finance.service;

import com.elshimma.erp.finance.dto.FinanceDashboardResponse;
import com.elshimma.erp.finance.dto.OrderFinancialSummaryResponse;
import com.elshimma.erp.order.entity.CustomerOrder;
import com.elshimma.erp.order.repository.CustomerOrderRepository;
import com.elshimma.erp.product.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final CustomerOrderRepository orderRepository;

    @Transactional(readOnly = true)
    public FinanceDashboardResponse getDashboard() {
        return FinanceDashboardResponse.builder()
                .totalRevenue(orderRepository.sumTotalRevenue())
                .totalProfit(orderRepository.sumTotalProfit())
                .totalPaid(orderRepository.sumTotalPaid())
                .totalUnpaid(orderRepository.sumTotalUnpaid())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderFinancialSummaryResponse getOrderSummary(Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", "id", orderId));

        return OrderFinancialSummaryResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .totalPaid(order.getDepositAmount())
                .remainingBalance(order.getRemainingAmount())
                .calculatedCost(order.getEstimatedCost())
                .calculatedProfit(order.getEstimatedProfit())
                .build();
    }
}
