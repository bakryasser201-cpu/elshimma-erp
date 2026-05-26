package com.elshimma.erp.hr.dto;

import com.elshimma.erp.hr.entity.LeaveRequestStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveRequestStatus status;
    private LocalDateTime createdAt;
}
