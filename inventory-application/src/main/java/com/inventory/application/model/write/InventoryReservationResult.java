package com.inventory.application.model.write;

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
