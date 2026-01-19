package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record LowStockAlertEvent(
        Id inventoryItemId,
        String sku,
        int currentQuantity,
        int reorderPoint,
        LocalDateTime occurredAt
) implements Event {
}
