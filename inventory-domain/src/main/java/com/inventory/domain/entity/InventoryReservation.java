package com.inventory.domain.entity;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.InventoryReservationStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class InventoryReservation extends Entity<Id> {

    private final Id inventoryItemId;
    private final String orderId;
    private final String orderLineId;
    private final int quantity;
    private final String idempotencyKey;
    private InventoryReservationStatus status;
    private LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InventoryReservation(
            Id id,
            Id inventoryItemId,
            String orderId,
            String orderLineId,
            int quantity,
            InventoryReservationStatus status,
            LocalDateTime expiresAt,
            String idempotencyKey,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        super(id);
        this.inventoryItemId = Objects.requireNonNull(inventoryItemId, "inventoryItemId is required");
        this.orderId = Objects.requireNonNull(orderId, "orderId is required");
        this.orderLineId = Objects.requireNonNull(orderLineId, "orderLineId is required");
        this.quantity = quantity;
        this.status = status == null ? InventoryReservationStatus.ACTIVE : status;
        this.expiresAt = expiresAt;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static InventoryReservation create(
            Id id,
            Id inventoryItemId,
            String orderId,
            String orderLineId,
            int quantity,
            LocalDateTime expiresAt,
            String idempotencyKey
    ) {
        return new InventoryReservation(
                id,
                inventoryItemId,
                orderId,
                orderLineId,
                quantity,
                InventoryReservationStatus.ACTIVE,
                expiresAt,
                idempotencyKey,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public void release() {
        this.status = InventoryReservationStatus.RELEASED;
        this.updatedAt = LocalDateTime.now();
    }

    public void fulfill() {
        this.status = InventoryReservationStatus.FULFILLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = InventoryReservationStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = InventoryReservationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == InventoryReservationStatus.ACTIVE;
    }
}
