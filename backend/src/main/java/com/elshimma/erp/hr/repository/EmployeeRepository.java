package com.elshimma.erp.hr.repository;

import com.elshimma.erp.hr.entity.Employee;
import com.elshimma.erp.hr.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<Employee> findByUserEmail(String email);

    @Query("""
            SELECT e FROM Employee e
            WHERE (:keyword IS NULL
                OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:status IS NULL OR e.status = :status)
            AND (:departmentId IS NULL OR e.department.id = :departmentId)
            """)
    Page<Employee> findWithFilters(
            @Param("keyword") String keyword,
            @Param("status") EmployeeStatus status,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );
}
