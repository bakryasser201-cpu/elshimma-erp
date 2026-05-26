package com.elshimma.erp.hr.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "positions", indexes = {
        @Index(name = "idx_position_title", columnList = "title", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
}
