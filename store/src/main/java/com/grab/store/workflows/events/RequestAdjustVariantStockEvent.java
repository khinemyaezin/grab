package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;
import com.inventory.domain.enums.AdjustmentReason;

import java.time.Instant;

public record RequestAdjustVariantStockEvent(
        String workflowId,
        String inventoryItemId,
        int newOnHandQuantity,
        AdjustmentReason reason,
        String createdBy,
        String scopeKey,
        String scopeId,
        Instant occurredAt,
        int version
) implements Event {
}
