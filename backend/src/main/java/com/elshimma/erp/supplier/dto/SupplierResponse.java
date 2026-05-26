package com.elshimma.erp.supplier.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierResponse {
    private Long id;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String paymentTerms;
    private BigDecimal rating;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
