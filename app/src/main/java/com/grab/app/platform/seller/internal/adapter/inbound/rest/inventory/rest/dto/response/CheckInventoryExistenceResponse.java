package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

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
