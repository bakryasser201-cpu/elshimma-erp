package com.elshimma.erp.product.repository;

import com.elshimma.erp.product.entity.Product;
import com.elshimma.erp.product.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategory(ProductCategory category, Pageable pageable);

    Page<Product> findByCategoryAndActiveTrue(ProductCategory category, Pageable pageable);

    /**
     * Case-insensitive search by product name.
     * Uses LOWER() for PostgreSQL compatibility.
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.active = true")
    Page<Product> searchByNameAndActiveTrue(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:category IS NULL OR p.category = :category)
            AND (:active IS NULL OR p.active = :active)
            """)
    Page<Product> findWithFilters(
            @Param("keyword") String keyword,
            @Param("category") ProductCategory category,
            @Param("active") Boolean active,
            Pageable pageable
    );

    boolean existsByNameIgnoreCase(String name);
}
