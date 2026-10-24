package com.grab.store.shared.events.inventory;

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
