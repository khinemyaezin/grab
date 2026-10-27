package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

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
