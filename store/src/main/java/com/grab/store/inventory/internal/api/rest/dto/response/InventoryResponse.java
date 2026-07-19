package com.grab.store.inventory.internal.api.rest.dto.response;

public record InventoryResponse(
        String id,
        String sku,
        String merchantId,
        String productVariantId,
        String productName,
        String locationId,
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
