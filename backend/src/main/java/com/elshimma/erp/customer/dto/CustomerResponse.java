package com.elshimma.erp.customer.dto;

import com.elshimma.erp.customer.entity.CustomerType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private String companyName;
    private CustomerType customerType;
    private String email;
    private String phone;
    private String address;
    private BigDecimal creditLimit;
    private String paymentTerms;
    private String notes;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
