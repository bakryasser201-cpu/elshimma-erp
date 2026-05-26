package com.elshimma.erp.customer.dto;

import com.elshimma.erp.customer.entity.CustomerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCustomerRequest {

    private String companyName;
    private CustomerType customerType;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;
    private String address;

    @DecimalMin(value = "0.00", message = "Credit limit cannot be negative")
    private BigDecimal creditLimit;

    private String paymentTerms;
    private String notes;
    private Boolean active;
}
