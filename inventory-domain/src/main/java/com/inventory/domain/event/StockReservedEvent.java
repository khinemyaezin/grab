package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record StockReservedEvent(
        Id inventoryItemId,
        String sku,
        int quantity,
        String orderId,
        LocalDateTime occurredAt
) implements Event {
}
