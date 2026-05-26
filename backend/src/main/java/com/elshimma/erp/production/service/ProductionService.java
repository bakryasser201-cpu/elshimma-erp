package com.elshimma.erp.production.service;

import com.elshimma.erp.order.entity.CustomerOrder;
import com.elshimma.erp.order.entity.OrderStatus;
import com.elshimma.erp.order.exception.InvalidOrderStateException;
import com.elshimma.erp.order.repository.CustomerOrderRepository;
import com.elshimma.erp.production.dto.ProductionStageHistoryResponse;
import com.elshimma.erp.production.dto.UpdateProductionStatusRequest;
import com.elshimma.erp.production.entity.ProductionStageHistory;
import com.elshimma.erp.production.entity.ProductionStatus;
import com.elshimma.erp.production.repository.ProductionStageHistoryRepository;
import com.elshimma.erp.product.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final CustomerOrderRepository orderRepository;
    private final ProductionStageHistoryRepository historyRepository;

    @Transactional
    public ProductionStageHistoryResponse updateProductionStatus(Long orderId, UpdateProductionStatusRequest request) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", "id", orderId));

        ProductionStatus newStatus = request.getProductionStatus();
        
        // Ensure order isn't cancelled
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Cannot update production status of a cancelled order.");
        }

        order.setProductionStatus(newStatus);
        order.setOrderStatus(OrderStatus.IN_PRODUCTION);
        
        // Update lifecycle timestamps
        if (newStatus != ProductionStatus.SAMPLE && newStatus != ProductionStatus.APPROVED && order.getProductionStartedAt() == null) {
            order.setProductionStartedAt(LocalDateTime.now());
        }
        
        if (newStatus == ProductionStatus.READY) {
            order.setReadyAt(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.READY);
        }

        orderRepository.save(order);

        ProductionStageHistory history = ProductionStageHistory.builder()
                .order(order)
                .productionStatus(newStatus)
                .notes(request.getNotes())
                .build();

        historyRepository.save(history);

        return mapToHistoryResponse(history);
    }

    @Transactional(readOnly = true)
    public List<ProductionStageHistoryResponse> getProductionHistory(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("CustomerOrder", "id", orderId);
        }
        
        return historyRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    private ProductionStageHistoryResponse mapToHistoryResponse(ProductionStageHistory history) {
        return ProductionStageHistoryResponse.builder()
                .id(history.getId())
                .orderId(history.getOrder().getId())
                .productionStatus(history.getProductionStatus())
                .notes(history.getNotes())
                .changedBy(history.getChangedBy())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
