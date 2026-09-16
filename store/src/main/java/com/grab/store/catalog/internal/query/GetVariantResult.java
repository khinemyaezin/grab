package com.grab.store.catalog.internal.query;

import java.util.List;

public record GetVariantResult(
        String productId,
        String productName,
        String variantId,
        String sku,
        String status,
        String matrixKey,
        List<Variation> variations,
        boolean manageInventory,
        List<String> mediaIds,
        String thumbnailMediaId
) {
    public record Variation(
            String optionId,
            String optionName,
            String typeId,
            String typeName
    ) {}
}
