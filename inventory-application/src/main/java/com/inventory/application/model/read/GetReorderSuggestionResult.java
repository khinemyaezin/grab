package com.inventory.application.model.read;

public record GetReorderSuggestionResult(
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
