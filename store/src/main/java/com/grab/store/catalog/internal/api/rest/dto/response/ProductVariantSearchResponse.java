package com.grab.store.catalog.internal.api.rest.dto.response;

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
