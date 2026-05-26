package com.elshimma.erp.hr.dto;

import com.elshimma.erp.hr.entity.EmployeeStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    private String phone;
    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.00", message = "Salary cannot be negative")
    private BigDecimal salary;
    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;
    private EmployeeStatus status;
    private Long departmentId;
    private Long positionId;
    private Long userId;
}
