package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.AdjustmentReason;

import java.time.LocalDateTime;

public record StockAdjustedEvent(
        Id inventoryItemId,
        String sku,
        int previousQuantity,
        int newQuantity,
        AdjustmentReason reason,
        LocalDateTime occurredAt
) implements Event {

    public int adjustmentAmount() {
        return newQuantity - previousQuantity;
    }
}
