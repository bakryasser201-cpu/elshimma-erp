package com.elshimma.erp.customer.mapper;

import com.elshimma.erp.customer.dto.CustomerResponse;
import com.elshimma.erp.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .companyName(customer.getCompanyName())
                .customerType(customer.getCustomerType())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .creditLimit(customer.getCreditLimit())
                .paymentTerms(customer.getPaymentTerms())
                .notes(customer.getNotes())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
