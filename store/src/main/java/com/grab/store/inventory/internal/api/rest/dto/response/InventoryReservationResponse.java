package com.grab.store.inventory.internal.api.rest.dto.response;

import java.time.LocalDateTime;

public record InventoryReservationResponse(
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
