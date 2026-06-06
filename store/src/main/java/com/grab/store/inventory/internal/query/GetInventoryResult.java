package com.grab.store.inventory.internal.query;

import com.grab.framework.id.Id;

public record GetInventoryResult(
        Id id,
        String sku,
        String productVariantId,
        Id locationId,
        int onHand,
        int reserved,
        int damaged,
        int available,
        String status,
        int safetyStock,
        int reorderPoint,
        int reorderQuantity,
        Integer maxStock
) {
}
