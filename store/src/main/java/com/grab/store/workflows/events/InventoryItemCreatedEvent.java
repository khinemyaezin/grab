package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record InventoryItemCreatedEvent(
        String workflowId,
        String inventoryItemId,
        String sku,
        String locationId,
        Instant occurredAt,
        int version
) implements Event {
}
