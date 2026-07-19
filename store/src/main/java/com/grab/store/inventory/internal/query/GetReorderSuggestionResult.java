package com.grab.store.inventory.internal.query;

public record GetReorderSuggestionResult(
        String inventoryItemId,
        String sku,
        String productVariantId,
        String locationId,
        int currentAvailable,
        int reorderPoint,
        int suggestedQuantity,
        String priority
) {
}
