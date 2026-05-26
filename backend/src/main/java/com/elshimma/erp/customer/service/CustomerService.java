package com.elshimma.erp.customer.service;

import com.elshimma.erp.customer.dto.*;
import com.elshimma.erp.customer.entity.Customer;
import com.elshimma.erp.customer.entity.CustomerType;
import com.elshimma.erp.customer.mapper.CustomerMapper;
import com.elshimma.erp.customer.repository.CustomerRepository;
import com.elshimma.erp.customer.validation.CustomerValidation;
import com.elshimma.erp.order.entity.CustomerOrder;
import com.elshimma.erp.order.repository.CustomerOrderRepository;
import com.elshimma.erp.product.exception.DuplicateResourceException;
import com.elshimma.erp.product.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerOrderRepository orderRepository;
    private final CustomerMapper customerMapper;
    private final CustomerValidation customerValidation;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        validateEmailUniqueness(request.getEmail(), null);

        Customer customer = Customer.builder()
                .companyName(request.getCompanyName())
                .customerType(request.getCustomerType())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .creditLimit(request.getCreditLimit())
                .paymentTerms(request.getPaymentTerms())
                .notes(request.getNotes())
                .active(true)
                .build();

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = findCustomerOrThrow(id);

        if (request.getCompanyName() != null) {
            customer.setCompanyName(request.getCompanyName());
        }
        if (request.getCustomerType() != null) {
            customer.setCustomerType(request.getCustomerType());
        }
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(customer.getEmail())) {
            validateEmailUniqueness(request.getEmail(), id);
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCreditLimit() != null) {
            customer.setCreditLimit(request.getCreditLimit());
        }
        if (request.getPaymentTerms() != null) {
            customer.setPaymentTerms(request.getPaymentTerms());
        }
        if (request.getNotes() != null) {
            customer.setNotes(request.getNotes());
        }
        if (request.getActive() != null) {
            customer.setActive(request.getActive());
        }

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findCustomerOrThrow(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return customerMapper.toResponse(findCustomerOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomers(
            String keyword,
            CustomerType customerType,
            Boolean active,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        String normalizedKeyword = customerValidation.normalizeSearchKeyword(keyword);

        return customerRepository.findWithFilters(normalizedKeyword, customerType, active, pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerOrderHistoryResponse> getOrderHistory(Long customerId, int page, int size) {
        findCustomerOrThrow(customerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByCustomerIdAndActiveTrue(customerId, pageable)
                .map(this::mapToOrderHistoryResponse);
    }

    @Transactional(readOnly = true)
    public CustomerBalanceSummaryResponse getBalanceSummary(Long customerId) {
        Customer customer = findCustomerOrThrow(customerId);
        BigDecimal totalOrders = orderRepository.sumTotalAmountByCustomerId(customerId);
        BigDecimal totalPaid = orderRepository.sumPaidAmountByCustomerId(customerId);
        BigDecimal outstandingBalance = orderRepository.sumOutstandingBalanceByCustomerId(customerId);
        BigDecimal availableCredit = customer.getCreditLimit().subtract(outstandingBalance);

        return CustomerBalanceSummaryResponse.builder()
                .customerId(customer.getId())
                .companyName(customer.getCompanyName())
                .creditLimit(customer.getCreditLimit())
                .totalOrders(totalOrders)
                .totalPaid(totalPaid)
                .outstandingBalance(outstandingBalance)
                .availableCredit(availableCredit)
                .build();
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    private void validateEmailUniqueness(String email, Long currentCustomerId) {
        if (email == null || email.isBlank()) {
            return;
        }
        boolean duplicate = customerRepository.existsByEmailIgnoreCase(email);
        if (duplicate && currentCustomerId == null) {
            throw new DuplicateResourceException("Customer", "email", email);
        }
        if (duplicate && currentCustomerId != null) {
            Optional<Customer> existing = customerRepository.findByEmailIgnoreCase(email);
            if (existing.isPresent() && !existing.get().getId().equals(currentCustomerId)) {
                throw new DuplicateResourceException("Customer", "email", email);
            }
        }
    }

    private CustomerOrderHistoryResponse mapToOrderHistoryResponse(CustomerOrder order) {
        return CustomerOrderHistoryResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .productionStatus(order.getProductionStatus())
                .totalAmount(order.getTotalAmount())
                .paidAmount(order.getDepositAmount())
                .remainingAmount(order.getRemainingAmount())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .actualDeliveryDate(order.getActualDeliveryDate())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
