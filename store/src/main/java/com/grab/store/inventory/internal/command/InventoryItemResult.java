package com.grab.store.inventory.internal.command;

public record InventoryItemResult(
        String id,
        String sku,
        String merchantId,
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
