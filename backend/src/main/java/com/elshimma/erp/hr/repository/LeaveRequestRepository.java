package com.elshimma.erp.hr.repository;

import com.elshimma.erp.hr.entity.LeaveRequest;
import com.elshimma.erp.hr.entity.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<LeaveRequest> findByStatus(LeaveRequestStatus status, Pageable pageable);
}
