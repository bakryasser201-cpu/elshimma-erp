package com.elshimma.erp.customer.dto;

import com.elshimma.erp.customer.entity.CustomerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCustomerRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Customer type is required")
    private CustomerType customerType;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;
    private String address;

    @NotNull(message = "Credit limit is required")
    @DecimalMin(value = "0.00", message = "Credit limit cannot be negative")
    private BigDecimal creditLimit;

    private String paymentTerms;
    private String notes;
}
