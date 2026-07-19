package com.grab.store.inventory.internal.command;

public record DiscontinueInventoryForDeletedVariantResult(
        String productVariantId,
        int discontinuedCount,
        int skippedCount
) {
}
