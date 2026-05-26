package com.elshimma.erp.warehouse.entity;

import com.elshimma.erp.inventory.entity.Warehouse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouse_locations", indexes = {
        @Index(name = "idx_location_warehouse_id", columnList = "warehouse_id"),
        @Index(name = "idx_location_bin_code", columnList = "bin_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    private String aisle;
    private String shelf;

    @Column(name = "bin_code", nullable = false)
    private String binCode;
}
