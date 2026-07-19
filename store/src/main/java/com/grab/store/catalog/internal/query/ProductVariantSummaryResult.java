package com.grab.store.catalog.internal.query;

public record ProductVariantSummaryResult(
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
