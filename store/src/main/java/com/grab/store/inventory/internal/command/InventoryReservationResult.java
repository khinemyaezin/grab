package com.grab.store.inventory.internal.command;

import java.time.LocalDateTime;

public record InventoryReservationResult(
        String id,
        String inventoryItemId,
        String orderId,
        String orderLineId,
        int quantity,
        String status,
        LocalDateTime expiresAt,
        String idempotencyKey
) {
}
