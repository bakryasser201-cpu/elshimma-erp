package com.elshimma.erp.hr.dto;

import com.elshimma.erp.hr.entity.EmployeeStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private BigDecimal salary;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private Long departmentId;
    private String departmentName;
    private Long positionId;
    private String positionTitle;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
