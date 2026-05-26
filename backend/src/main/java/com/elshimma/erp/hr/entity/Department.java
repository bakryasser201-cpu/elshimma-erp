package com.elshimma.erp.hr.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments", indexes = {
        @Index(name = "idx_department_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
}
