package com.grab.store.inventory.internal.query;

import java.time.LocalDateTime;
import java.util.List;

public record GetInventoryReservationsResult(
        String inventoryItemId,
        List<Reservation> reservations
) {
    public record Reservation(
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
}
