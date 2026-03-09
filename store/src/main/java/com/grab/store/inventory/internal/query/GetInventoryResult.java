package com.grab.store.inventory.internal.query;

public record GetInventoryResult(
        String id,
        String sku,
        String productVariantId,
        String locationId,
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
