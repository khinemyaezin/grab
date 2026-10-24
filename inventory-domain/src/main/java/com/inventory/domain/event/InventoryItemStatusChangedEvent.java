package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.InventoryStatus;

import java.time.LocalDateTime;

public record InventoryItemStatusChangedEvent(
        Id inventoryItemId,
        String sku,
        InventoryStatus status,
        LocalDateTime occurredAt
) implements Event {
}
