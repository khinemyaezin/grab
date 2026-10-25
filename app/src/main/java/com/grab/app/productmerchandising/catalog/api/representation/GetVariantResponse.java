package com.grab.app.productmerchandising.catalog.api.representation;

import java.util.List;

public record GetVariantResponse(
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
