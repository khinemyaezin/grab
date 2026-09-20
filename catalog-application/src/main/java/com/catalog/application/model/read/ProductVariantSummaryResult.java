package com.catalog.application.model.read;

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
