package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record RequestSyncInventoryItemEvent(
        String workflowId,
        String sku,
        String merchantId,
        String locationId,
        String inventoryItemId,
        InventorySyncOp op,
        InventorySyncPayload.CreateStock create,
        InventorySyncPayload.AdjustStock adjust,
        InventorySyncPayload.DamageStock damage,
        InventorySyncPayload.WriteOffStock writeOff,
        InventorySyncPayload.Reorder reorder,
        String createdBy,
        String scopeKey,
        String scopeId,
        Instant occurredAt,
        int version
) implements Event {
}
