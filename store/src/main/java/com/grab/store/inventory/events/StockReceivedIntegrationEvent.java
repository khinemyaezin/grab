package com.grab.store.inventory.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record StockReceivedIntegrationEvent(
        String inventoryItemId,
        String sku,
        int quantity,
        String locationId,
        Instant occurredAt,
        int version
) implements Event {
}
