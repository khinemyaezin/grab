package com.grab.store.inventory.internal.query;

import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record GetInventoryReservationsResult(
        Id id,
        Id inventoryItemId,
        String orderId,
        String orderLineId,
        int quantity,
        String status,
        LocalDateTime expiresAt,
        String idempotencyKey
) {
}
