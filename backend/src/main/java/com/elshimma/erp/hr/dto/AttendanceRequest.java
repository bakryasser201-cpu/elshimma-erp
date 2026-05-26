package com.elshimma.erp.hr.dto;

import com.elshimma.erp.hr.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    @NotNull(message = "Check-in is required")
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;
}
