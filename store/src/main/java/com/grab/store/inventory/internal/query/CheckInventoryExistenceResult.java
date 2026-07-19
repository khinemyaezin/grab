package com.grab.store.inventory.internal.query;

import com.grab.framework.id.Id;

import java.util.List;

public record CheckInventoryExistenceResult(
        List<Entry> items
) {
    public record Entry(
            String sku,
            boolean exists,
            Id inventoryItemId
    ) {
    }
}
