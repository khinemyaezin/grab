package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record InventoryItemDiscontinuedEvent(
        Id inventoryItemId,
        String sku,
        String productVariantId,
        Id locationId,
        LocalDateTime occurredAt
) implements Event {
}
