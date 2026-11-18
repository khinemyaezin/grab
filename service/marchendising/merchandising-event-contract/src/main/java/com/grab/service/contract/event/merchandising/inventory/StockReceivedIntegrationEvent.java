package com.grab.service.contract.event.merchandising.inventory;

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
