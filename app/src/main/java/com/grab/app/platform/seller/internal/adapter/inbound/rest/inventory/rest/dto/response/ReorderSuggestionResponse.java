package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

public record ReorderSuggestionResponse(
        String inventoryItemId,
        String sku,
        String productName,
        String productVariantId,
        String locationId,
        int currentAvailable,
        int reorderPoint,
        int suggestedQuantity,
        String priority
) {
}
