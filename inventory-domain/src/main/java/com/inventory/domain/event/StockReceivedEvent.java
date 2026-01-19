package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record StockReceivedEvent(
        Id inventoryItemId,
        String sku,
        int quantity,
        Id locationId,
        LocalDateTime occurredAt
) implements Event {
}
