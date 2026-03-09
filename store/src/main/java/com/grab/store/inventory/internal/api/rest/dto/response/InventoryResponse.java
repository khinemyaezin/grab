package com.grab.store.inventory.internal.api.rest.dto.response;

public record InventoryResponse(
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
