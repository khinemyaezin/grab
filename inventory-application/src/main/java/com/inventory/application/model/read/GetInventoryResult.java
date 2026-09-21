package com.inventory.application.model.read;

import com.grab.framework.id.Id;

public record GetInventoryResult(
        Id id,
        String sku,
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
