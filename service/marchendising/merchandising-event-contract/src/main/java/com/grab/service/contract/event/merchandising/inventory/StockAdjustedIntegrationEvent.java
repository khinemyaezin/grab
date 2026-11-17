package com.grab.service.contract.event.merchandising.inventory;

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
