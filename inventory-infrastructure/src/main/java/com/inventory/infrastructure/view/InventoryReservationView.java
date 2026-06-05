package com.inventory.infrastructure.view;

import com.inventory.domain.enums.InventoryReservationStatus;

import java.time.LocalDateTime;

public record InventoryReservationView(
        String uuid,
        String inventoryItemUuid,
        String orderId,
        String orderLineId,
        int quantity,
        InventoryReservationStatus status,
        LocalDateTime expiresAt,
        String idempotencyKey,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
