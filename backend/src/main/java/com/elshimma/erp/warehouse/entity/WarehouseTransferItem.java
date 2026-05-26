package com.elshimma.erp.warehouse.entity;

import com.elshimma.erp.product.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "warehouse_transfer_items", indexes = {
        @Index(name = "idx_transfer_item_transfer_id", columnList = "transfer_id"),
        @Index(name = "idx_transfer_item_variant_id", columnList = "product_variant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransferItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private WarehouseTransfer transfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal quantity;
}
