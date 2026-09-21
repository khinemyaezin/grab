package com.inventory.application.model.write;

public record DiscontinueInventoryForDeletedVariantResult(
        String productVariantId,
        int discontinuedCount,
        int skippedCount
) {
}
