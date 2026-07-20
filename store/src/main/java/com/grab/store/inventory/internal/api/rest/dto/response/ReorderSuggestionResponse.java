package com.grab.store.inventory.internal.api.rest.dto.response;

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
