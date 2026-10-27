package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

public record ProductVariantSearchResponse(
        String productId,
        String variantId,
        String sku,
        String productName,
        String status,
        String slug,
        String categoryName,
        String categoryId
) {
}
