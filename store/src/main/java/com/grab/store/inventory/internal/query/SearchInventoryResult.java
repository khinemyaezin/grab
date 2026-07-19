package com.grab.store.inventory.internal.query;

import com.grab.framework.id.Id;

public record SearchInventoryResult(
        Id id,
        String sku,
        Id merchantId,
        String productName,
        Id locationId,
        String locationCode,
        String locationName,
        int onHand,
        int reserved,
        int inTransit,
        int damaged,
        int available,
        String status,
        int safetyStock,
        int reorderPoint,
        int reorderQuantity,
        Integer maxStock
) {
}
