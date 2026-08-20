package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record InventoryItemSyncedEvent(
        String workflowId,
        String inventoryItemId,
        String sku,
        String locationId,
        boolean created,
        Instant occurredAt,
        int version
) implements Event {
}
