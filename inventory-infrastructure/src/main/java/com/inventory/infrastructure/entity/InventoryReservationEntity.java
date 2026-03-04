package com.inventory.infrastructure.entity;

import com.inventory.domain.enums.InventoryReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "inventory_reservation", indexes = {
        @Index(name = "idx_inventory_reservation_inventory", columnList = "inventory_item_uuid"),
        @Index(name = "idx_inventory_reservation_order", columnList = "order_id"),
        @Index(name = "idx_inventory_reservation_idempotency", columnList = "idempotency_key", unique = true)
})
public class InventoryReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(name = "inventory_item_uuid", nullable = false)
    private String inventoryItemUuid;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "order_line_id", nullable = false)
    private String orderLineId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryReservationStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
