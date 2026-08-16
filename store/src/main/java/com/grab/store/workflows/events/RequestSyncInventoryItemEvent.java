package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record RequestSyncInventoryItemEvent(
        String workflowId,
        String sku,
        String merchantId,
        String locationId,
        String inventoryItemId,
        int onHandQuantity,
        Integer safetyStock,
        Integer reorderPoint,
        Integer reorderQuantity,
        Integer maxStock,
        String createdBy,
        String scopeKey,
        String scopeId,
        Instant occurredAt,
        int version
) implements Event {
}
