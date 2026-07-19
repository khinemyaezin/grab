package com.grab.store.inventory.internal.api.rest.dto.response;

import java.util.List;

public record CheckInventoryExistenceResponse(
        List<Entry> items
) {
    public record Entry(
            String sku,
            boolean exists,
            String inventoryItemId
    ) {
    }
}
