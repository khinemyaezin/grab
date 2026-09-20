package com.grab.store.inventory.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record StockShippedIntegrationEvent(
        String inventoryItemId,
        String sku,
        int quantity,
        String orderId,
        Instant occurredAt,
        int version
) implements Event {
}
