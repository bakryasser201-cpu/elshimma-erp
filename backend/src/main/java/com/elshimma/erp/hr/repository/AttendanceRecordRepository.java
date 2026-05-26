package com.elshimma.erp.hr.repository;

import com.elshimma.erp.hr.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Page<AttendanceRecord> findByEmployeeId(Long employeeId, Pageable pageable);
}
