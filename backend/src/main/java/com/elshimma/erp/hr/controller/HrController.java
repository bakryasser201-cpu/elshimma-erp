package com.elshimma.erp.hr.controller;

import com.elshimma.erp.hr.dto.*;
import com.elshimma.erp.hr.entity.EmployeeStatus;
import com.elshimma.erp.hr.entity.LeaveRequestStatus;
import com.elshimma.erp.hr.service.HrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@Tag(name = "HR", description = "Employees, departments, attendance, leave, and self-service APIs")
public class HrController {

    private final HrService hrService;

    @PostMapping("/departments")
    @Operation(summary = "Create department")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrService.createDepartment(request));
    }

    @GetMapping("/departments")
    @Operation(summary = "List departments")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<DepartmentResponse>> getDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(hrService.getDepartments(page, size));
    }

    @PostMapping("/positions")
    @Operation(summary = "Create position")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<PositionResponse> createPosition(@Valid @RequestBody PositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrService.createPosition(request));
    }

    @GetMapping("/positions")
    @Operation(summary = "List positions")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<PositionResponse>> getPositions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(hrService.getPositions(page, size));
    }

    @PostMapping("/employees")
    @Operation(summary = "Create employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrService.createEmployee(request));
    }

    @GetMapping("/employees")
    @Operation(summary = "List employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<Page<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(hrService.getEmployees(keyword, status, departmentId, page, size));
    }

    @GetMapping("/employees/{id}")
    @Operation(summary = "Get employee by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(hrService.getEmployeeById(id));
    }

    @PutMapping("/employees/{id}")
    @Operation(summary = "Update employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(hrService.updateEmployee(id, request));
    }

    @DeleteMapping("/employees/{id}")
    @Operation(summary = "Deactivate employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        hrService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/attendance")
    @Operation(summary = "Create attendance record")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<AttendanceResponse> createAttendance(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrService.createAttendance(request));
    }

    @GetMapping("/attendance")
    @Operation(summary = "List attendance records")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<Page<AttendanceResponse>> getAttendance(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(hrService.getAttendance(employeeId, page, size));
    }

    @PostMapping("/leave-requests")
    @Operation(summary = "Create leave request")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    public ResponseEntity<LeaveResponse> createLeaveRequest(
            @Valid @RequestBody LeaveRequestDto request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrService.createLeaveRequest(request, authentication.getName()));
    }

    @GetMapping("/leave-requests")
    @Operation(summary = "List leave requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<Page<LeaveResponse>> getLeaveRequests(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(hrService.getLeaveRequests(employeeId, status, page, size));
    }

    @PatchMapping("/leave-requests/{id}/status")
    @Operation(summary = "Approve or reject leave request")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<LeaveResponse> updateLeaveStatus(
            @PathVariable Long id,
            @RequestParam LeaveRequestStatus status) {
        return ResponseEntity.ok(hrService.updateLeaveStatus(id, status));
    }

    @GetMapping("/me")
    @Operation(summary = "Get employee self profile")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<EmployeeResponse> getSelfProfile(Authentication authentication) {
        return ResponseEntity.ok(hrService.getSelfProfile(authentication.getName()));
    }

    @GetMapping("/me/leave-requests")
    @Operation(summary = "Get employee self leave history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<Page<LeaveResponse>> getSelfLeaveHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(hrService.getSelfLeaveHistory(authentication.getName(), page, size));
    }
}
