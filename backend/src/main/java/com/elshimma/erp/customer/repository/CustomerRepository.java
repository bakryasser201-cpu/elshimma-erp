package com.elshimma.erp.customer.repository;

import com.elshimma.erp.customer.entity.Customer;
import com.elshimma.erp.customer.entity.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);

    @Query("""
            SELECT c FROM Customer c
            WHERE (:keyword IS NULL
                OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:customerType IS NULL OR c.customerType = :customerType)
            AND (:active IS NULL OR c.active = :active)
            """)
    Page<Customer> findWithFilters(
            @Param("keyword") String keyword,
            @Param("customerType") CustomerType customerType,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
