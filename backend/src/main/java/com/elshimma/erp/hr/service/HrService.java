package com.elshimma.erp.hr.service;

import com.elshimma.erp.hr.dto.*;
import com.elshimma.erp.hr.entity.*;
import com.elshimma.erp.hr.exception.InvalidLeaveRequestStateException;
import com.elshimma.erp.hr.repository.*;
import com.elshimma.erp.product.exception.DuplicateResourceException;
import com.elshimma.erp.product.exception.ResourceNotFoundException;
import com.elshimma.erp.user.entity.User;
import com.elshimma.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HrService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }
        return mapToDepartmentResponse(departmentRepository.save(Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build()));
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getDepartments(int page, int size) {
        return departmentRepository.findAll(PageRequest.of(page, size, Sort.by("name").ascending()))
                .map(this::mapToDepartmentResponse);
    }

    @Transactional
    public PositionResponse createPosition(PositionRequest request) {
        if (positionRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new DuplicateResourceException("Position", "title", request.getTitle());
        }
        return mapToPositionResponse(positionRepository.save(Position.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build()));
    }

    @Transactional(readOnly = true)
    public Page<PositionResponse> getPositions(int page, int size) {
        return positionRepository.findAll(PageRequest.of(page, size, Sort.by("title").ascending()))
                .map(this::mapToPositionResponse);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }
        Employee employee = buildEmployee(new Employee(), request);
        employee.setStatus(request.getStatus() != null ? request.getStatus() : EmployeeStatus.ACTIVE);
        return mapToEmployeeResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = findEmployeeOrThrow(id);
        buildEmployee(employee, request);
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }
        return mapToEmployeeResponse(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getEmployees(String keyword, EmployeeStatus status, Long departmentId, int page, int size) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return employeeRepository.findWithFilters(
                        normalizedKeyword, status, departmentId, PageRequest.of(page, size, Sort.by("lastName").ascending()))
                .map(this::mapToEmployeeResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return mapToEmployeeResponse(findEmployeeOrThrow(id));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeOrThrow(id);
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
    }

    @Transactional
    public AttendanceResponse createAttendance(AttendanceRequest request) {
        Employee employee = findEmployeeOrThrow(request.getEmployeeId());
        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .status(request.getStatus())
                .build();
        return mapToAttendanceResponse(attendanceRepository.save(record));
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendance(Long employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("checkIn").descending());
        Page<AttendanceRecord> records = employeeId != null
                ? attendanceRepository.findByEmployeeId(employeeId, pageable)
                : attendanceRepository.findAll(pageable);
        return records.map(this::mapToAttendanceResponse);
    }

    @Transactional
    public LeaveResponse createLeaveRequest(LeaveRequestDto request, String authenticatedEmail) {
        Employee employee = request.getEmployeeId() != null
                ? findEmployeeOrThrow(request.getEmployeeId())
                : findEmployeeByUserEmailOrThrow(authenticatedEmail);
        LeaveRequest leave = LeaveRequest.builder()
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveRequestStatus.PENDING)
                .build();
        return mapToLeaveResponse(leaveRequestRepository.save(leave));
    }

    @Transactional(readOnly = true)
    public Page<LeaveResponse> getLeaveRequests(Long employeeId, LeaveRequestStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LeaveRequest> leaves = employeeId != null
                ? leaveRequestRepository.findByEmployeeId(employeeId, pageable)
                : status != null ? leaveRequestRepository.findByStatus(status, pageable)
                : leaveRequestRepository.findAll(pageable);
        return leaves.map(this::mapToLeaveResponse);
    }

    @Transactional
    public LeaveResponse updateLeaveStatus(Long id, LeaveRequestStatus status) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        if (leave.getStatus() != LeaveRequestStatus.PENDING) {
            throw new InvalidLeaveRequestStateException("Only PENDING leave requests can be updated");
        }
        if (status == LeaveRequestStatus.PENDING) {
            throw new InvalidLeaveRequestStateException("Leave request must be approved or rejected");
        }
        leave.setStatus(status);
        return mapToLeaveResponse(leaveRequestRepository.save(leave));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getSelfProfile(String email) {
        return mapToEmployeeResponse(findEmployeeByUserEmailOrThrow(email));
    }

    @Transactional(readOnly = true)
    public Page<LeaveResponse> getSelfLeaveHistory(String email, int page, int size) {
        Employee employee = findEmployeeByUserEmailOrThrow(email);
        return leaveRequestRepository.findByEmployeeId(
                        employee.getId(), PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToLeaveResponse);
    }

    private Employee buildEmployee(Employee employee, EmployeeRequest request) {
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
        employee.setDepartment(request.getDepartmentId() != null ? findDepartmentOrThrow(request.getDepartmentId()) : null);
        employee.setPosition(request.getPositionId() != null ? findPositionOrThrow(request.getPositionId()) : null);
        employee.setUser(request.getUserId() != null ? findUserOrThrow(request.getUserId()) : null);
        return employee;
    }

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    private Employee findEmployeeByUserEmailOrThrow(String email) {
        return employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "userEmail", email));
    }

    private Department findDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    private Position findPositionOrThrow(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private DepartmentResponse mapToDepartmentResponse(Department department) {
        return DepartmentResponse.builder().id(department.getId()).name(department.getName()).description(department.getDescription()).build();
    }

    private PositionResponse mapToPositionResponse(Position position) {
        return PositionResponse.builder().id(position.getId()).title(position.getTitle()).description(position.getDescription()).build();
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId()).firstName(employee.getFirstName()).lastName(employee.getLastName())
                .email(employee.getEmail()).phone(employee.getPhone()).salary(employee.getSalary())
                .hireDate(employee.getHireDate()).status(employee.getStatus())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .positionId(employee.getPosition() != null ? employee.getPosition().getId() : null)
                .positionTitle(employee.getPosition() != null ? employee.getPosition().getTitle() : null)
                .userId(employee.getUser() != null ? employee.getUser().getId() : null)
                .createdAt(employee.getCreatedAt()).updatedAt(employee.getUpdatedAt())
                .build();
    }

    private AttendanceResponse mapToAttendanceResponse(AttendanceRecord record) {
        return AttendanceResponse.builder()
                .id(record.getId()).employeeId(record.getEmployee().getId())
                .employeeName(record.getEmployee().getFirstName() + " " + record.getEmployee().getLastName())
                .checkIn(record.getCheckIn()).checkOut(record.getCheckOut()).status(record.getStatus())
                .build();
    }

    private LeaveResponse mapToLeaveResponse(LeaveRequest leave) {
        return LeaveResponse.builder()
                .id(leave.getId()).employeeId(leave.getEmployee().getId())
                .employeeName(leave.getEmployee().getFirstName() + " " + leave.getEmployee().getLastName())
                .startDate(leave.getStartDate()).endDate(leave.getEndDate())
                .reason(leave.getReason()).status(leave.getStatus()).createdAt(leave.getCreatedAt())
                .build();
    }
}
