package com.elshimma.erp.supplier.repository;

import com.elshimma.erp.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            SELECT s FROM Supplier s
            WHERE (:keyword IS NULL
                OR LOWER(s.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:active IS NULL OR s.active = :active)
            """)
    Page<Supplier> findWithFilters(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
