package com.elshimma.erp.order.service;

import com.elshimma.erp.customer.entity.Customer;
import com.elshimma.erp.customer.exception.CreditLimitExceededException;
import com.elshimma.erp.customer.repository.CustomerRepository;
import com.elshimma.erp.inventory.dto.StockMovementRequest;
import com.elshimma.erp.inventory.entity.Inventory;
import com.elshimma.erp.inventory.exception.InsufficientStockException;
import com.elshimma.erp.inventory.repository.InventoryRepository;
import com.elshimma.erp.inventory.service.InventoryService;
import com.elshimma.erp.order.dto.*;
import com.elshimma.erp.order.entity.*;
import com.elshimma.erp.order.exception.InvalidOrderStateException;
import com.elshimma.erp.order.repository.*;
import com.elshimma.erp.product.entity.ProductVariant;
import com.elshimma.erp.product.exception.ResourceNotFoundException;
import com.elshimma.erp.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderPaymentRepository paymentRepository;
    private final ProductVariantRepository variantRepository;
    
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    // ═══════════════════════════════════════════════════════════════
    //  CLIENT CRUD
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        Client client = Client.builder()
                .companyName(request.getCompanyName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .notes(request.getNotes())
                .active(true)
                .build();
        return mapToClientResponse(clientRepository.save(client));
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> getClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("companyName").ascending());
        return clientRepository.findByActiveTrue(pageable).map(this::mapToClientResponse);
    }

    // ═══════════════════════════════════════════════════════════════
    //  ORDER CRUD & STOCK RESERVATION
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = null;
        Client client = null;

        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

            if (!customer.isActive()) {
                throw new InvalidOrderStateException("Cannot create order for inactive customer " + customer.getId());
            }
        } else {
            client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));
        }

        String orderNumber = "ORD-" + LocalDate.now().toString().replace("-", "") + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        CustomerOrder order = CustomerOrder.builder()
                .orderNumber(orderNumber)
                .customer(customer)
                .client(client)
                .orderStatus(OrderStatus.PENDING)
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .notes(request.getNotes())
                .active(true)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal estimatedCost = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemReq.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", itemReq.getProductVariantId()));

            BigDecimal quantityToReserve = itemReq.getQuantity();
            BigDecimal itemTotalPrice = variant.getSellPrice().multiply(quantityToReserve);
            BigDecimal unitCost = variant.getCostPrice();
            BigDecimal itemTotalCost = unitCost.multiply(quantityToReserve);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productVariant(variant)
                    .productNameSnapshot(variant.getProduct() != null ? variant.getProduct().getName() : "Unknown")
                    .variantNameSnapshot(variant.getColor() + " " + variant.getSize())
                    .quantity(quantityToReserve)
                    .unitPrice(variant.getSellPrice())
                    .unitCostAtOrderTime(unitCost)
                    .totalPrice(itemTotalPrice)
                    .reservedQuantity(quantityToReserve)
                    .notes(itemReq.getNotes())
                    .build();

            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(itemTotalPrice);
            estimatedCost = estimatedCost.add(itemTotalCost);

            // Reserve Stock - Deterministic Strategy (Lowest Stock First)
            reserveStockAcrossWarehouses(variant.getId(), quantityToReserve, orderNumber);
        }

        order.setTotalAmount(totalAmount);
        order.setEstimatedCost(estimatedCost);
        order.setEstimatedProfit(totalAmount.subtract(estimatedCost));
        
        BigDecimal deposit = request.getExpectedDeposit() != null ? request.getExpectedDeposit() : BigDecimal.ZERO;
        order.setDepositAmount(deposit);
        order.setRemainingAmount(totalAmount.subtract(deposit));
        if (customer != null) {
            validateCustomerCreditLimit(customer, order.getRemainingAmount());
        }

        CustomerOrder savedOrder = orderRepository.save(order);

        // If a deposit was provided, automatically log it as a payment
        if (deposit.compareTo(BigDecimal.ZERO) > 0) {
            OrderPayment payment = OrderPayment.builder()
                    .order(savedOrder)
                    .amount(deposit)
                    .paymentMethod("INITIAL_DEPOSIT")
                    .paymentDate(LocalDateTime.now())
                    .notes("Initial deposit at order creation")
                    .build();
            savedOrder.getPayments().add(payment);
            paymentRepository.save(payment);
        }

        return mapToOrderResponse(savedOrder);
    }

    private void reserveStockAcrossWarehouses(Long variantId, BigDecimal requiredQuantity, String orderNumber) {
        List<Inventory> inventories = inventoryRepository.findByProductVariantIdAndActiveTrue(variantId)
                .stream()
                .filter(inv -> inv.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(Inventory::getAvailableQuantity)) // Lowest stock first
                .collect(Collectors.toList());

        BigDecimal remainingToReserve = requiredQuantity;

        for (Inventory inv : inventories) {
            if (remainingToReserve.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal available = inv.getAvailableQuantity();
            BigDecimal amountToReserveFromThisInv = remainingToReserve.min(available);

            StockMovementRequest moveReq = StockMovementRequest.builder()
                    .quantity(amountToReserveFromThisInv)
                    .reason("Order Reservation")
                    .referenceNumber(orderNumber)
                    .notes("Automatic reservation for order " + orderNumber)
                    .build();

            inventoryService.reserveStock(inv.getId(), moveReq);
            remainingToReserve = remainingToReserve.subtract(amountToReserveFromThisInv);
        }

        if (remainingToReserve.compareTo(BigDecimal.ZERO) > 0) {
            // Business rule: If we strictly require stock, throw exception. 
            // In many ERPs, order is allowed but stock becomes backordered. 
            // For now, based on "if stock exists", we just throw if insufficient for the whole order.
            throw new InsufficientStockException("Insufficient stock to fulfill order for variant " + variantId + ". Short by: " + remainingToReserve);
        }
    }

    @Transactional
    public OrderResponse addPayment(Long orderId, OrderPaymentRequest request) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", "id", orderId));

        OrderPayment payment = OrderPayment.builder()
                .order(order)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(request.getPaymentDate())
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .build();

        paymentRepository.save(payment);
        order.getPayments().add(payment);

        order.setDepositAmount(order.getDepositAmount().add(request.getAmount()));
        order.setRemainingAmount(order.getTotalAmount().subtract(order.getDepositAmount()));
        
        return mapToOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", "id", orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return mapToOrderResponse(order);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        // Release all reserved stock
        for (OrderItem item : order.getItems()) {
            releaseStockAcrossWarehouses(item.getProductVariant().getId(), item.getReservedQuantity(), order.getOrderNumber());
            item.setReservedQuantity(BigDecimal.ZERO);
        }

        return mapToOrderResponse(orderRepository.save(order));
    }

    private void releaseStockAcrossWarehouses(Long variantId, BigDecimal quantityToRelease, String orderNumber) {
        List<Inventory> inventories = inventoryRepository.findByProductVariantIdAndActiveTrue(variantId)
                .stream()
                .filter(inv -> inv.getReservedQuantity().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getReservedQuantity().compareTo(a.getReservedQuantity())) // highest reserved first
                .collect(Collectors.toList());

        BigDecimal remainingToRelease = quantityToRelease;

        for (Inventory inv : inventories) {
            if (remainingToRelease.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal reserved = inv.getReservedQuantity();
            BigDecimal amountToReleaseFromThisInv = remainingToRelease.min(reserved);

            StockMovementRequest moveReq = StockMovementRequest.builder()
                    .quantity(amountToReleaseFromThisInv)
                    .reason("Order Cancellation")
                    .referenceNumber(orderNumber)
                    .notes("Automatic release for cancelled order " + orderNumber)
                    .build();

            inventoryService.releaseStock(inv.getId(), moveReq);
            remainingToRelease = remainingToRelease.subtract(amountToReleaseFromThisInv);
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Long customerId, Long clientId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (customerId != null) {
            return orderRepository.findByCustomerIdAndActiveTrue(customerId, pageable).map(this::mapToOrderResponse);
        } else if (clientId != null) {
            return orderRepository.findByClientIdAndActiveTrue(clientId, pageable).map(this::mapToOrderResponse);
        } else if (status != null) {
            return orderRepository.findByOrderStatusAndActiveTrue(status, pageable).map(this::mapToOrderResponse);
        }
        return orderRepository.findByActiveTrue(pageable).map(this::mapToOrderResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> getDelayedOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expectedDeliveryDate").ascending());
        return orderRepository.findDelayedOrders(LocalDate.now(), pageable).map(this::mapToOrderResponse);
    }

    // ═══════════════════════════════════════════════════════════════
    //  MAPPERS
    // ═══════════════════════════════════════════════════════════════

    private ClientResponse mapToClientResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .companyName(client.getCompanyName())
                .contactPerson(client.getContactPerson())
                .phone(client.getPhone())
                .email(client.getEmail())
                .address(client.getAddress())
                .notes(client.getNotes())
                .active(client.isActive())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }

    private void validateCustomerCreditLimit(Customer customer, BigDecimal newOrderRemainingAmount) {
        BigDecimal currentOutstanding = orderRepository.sumOutstandingBalanceByCustomerId(customer.getId());
        BigDecimal projectedOutstanding = currentOutstanding.add(newOrderRemainingAmount);

        if (projectedOutstanding.compareTo(customer.getCreditLimit()) > 0) {
            throw new CreditLimitExceededException(
                    "Customer credit limit exceeded. Limit: " + customer.getCreditLimit()
                            + ", outstanding: " + currentOutstanding
                            + ", new order balance: " + newOrderRemainingAmount);
        }
    }

    private OrderResponse mapToOrderResponse(CustomerOrder order) {
        List<OrderItemResponse> items = order.getItems().stream().map(this::mapToOrderItemResponse).collect(Collectors.toList());
        List<OrderPaymentResponse> payments = order.getPayments().stream().map(this::mapToPaymentResponse).collect(Collectors.toList());
        Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;
        String customerCompanyName = order.getCustomer() != null ? order.getCustomer().getCompanyName() : null;
        Long legacyClientId = order.getClient() != null ? order.getClient().getId() : customerId;
        String companyName = order.getClient() != null ? order.getClient().getCompanyName() : customerCompanyName;

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(customerId)
                .customerCompanyName(customerCompanyName)
                .clientId(legacyClientId)
                .companyName(companyName)
                .orderStatus(order.getOrderStatus())
                .productionStatus(order.getProductionStatus())
                .totalAmount(order.getTotalAmount())
                .depositAmount(order.getDepositAmount())
                .remainingAmount(order.getRemainingAmount())
                .estimatedCost(order.getEstimatedCost())
                .estimatedProfit(order.getEstimatedProfit())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .actualDeliveryDate(order.getActualDeliveryDate())
                .productionStartedAt(order.getProductionStartedAt())
                .readyAt(order.getReadyAt())
                .deliveredAt(order.getDeliveredAt())
                .notes(order.getNotes())
                .active(order.isActive())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .payments(payments)
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productVariantId(item.getProductVariant().getId())
                .variantSku(item.getProductVariant().getSku())
                .productNameSnapshot(item.getProductNameSnapshot())
                .variantNameSnapshot(item.getVariantNameSnapshot())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .unitCostAtOrderTime(item.getUnitCostAtOrderTime())
                .totalPrice(item.getTotalPrice())
                .reservedQuantity(item.getReservedQuantity())
                .notes(item.getNotes())
                .build();
    }
    
    private OrderPaymentResponse mapToPaymentResponse(OrderPayment payment) {
        return OrderPaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .referenceNumber(payment.getReferenceNumber())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
