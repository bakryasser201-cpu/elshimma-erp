package com.elshimma.erp.supplier.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;
    private String contactPerson;
    @Email(message = "Email must be valid")
    private String email;
    private String phone;
    private String address;
    private String paymentTerms;
    @DecimalMin(value = "0.00", message = "Rating cannot be negative")
    @DecimalMax(value = "5.00", message = "Rating cannot exceed 5")
    private BigDecimal rating;
    private Boolean active;
}
