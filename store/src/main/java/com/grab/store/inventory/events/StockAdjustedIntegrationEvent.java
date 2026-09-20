package com.grab.store.inventory.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record StockAdjustedIntegrationEvent(
        String inventoryItemId,
        String sku,
        int previousQuantity,
        int newQuantity,
        String reason,
        Instant occurredAt,
        int version
) implements Event {
}
