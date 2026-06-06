package com.inventory.infrastructure.entity;

import com.inventory.domain.enums.InventoryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "inventory_item", indexes = {
        @Index(name = "idx_inventory_sku", columnList = "sku"),
        @Index(name = "idx_inventory_location", columnList = "location_id"),
        @Index(name = "idx_inventory_seller", columnList = "seller_id"),
        @Index(name = "idx_inventory_sku_location", columnList = "sku, location_id", unique = true)
})
public class InventoryItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(nullable = false)
    private String sku;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(name = "product_variant_id")
    private String productVariantId;

    @Column(name = "location_id", nullable = false)
    private String locationId;

    @Column(name = "on_hand", nullable = false)
    private int onHand = 0;

    @Column(nullable = false)
    private int reserved = 0;

    @Column(name = "in_transit", nullable = false)
    private int inTransit= 0;

    @Column(nullable = false)
    private int damaged= 0;

    @Column(name = "safety_stock", nullable = false)
    private int safetyStock= 0;

    @Column(name = "reorder_point", nullable = false)
    private int reorderPoint= 0;

    @Column(name = "reorder_quantity", nullable = false)
    private int reorderQuantity= 0;

    @Column(name = "max_stock")
    private Integer maxStock= 0;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }
}
