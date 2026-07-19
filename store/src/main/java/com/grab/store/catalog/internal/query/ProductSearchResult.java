package com.grab.store.catalog.internal.query;

public record ProductSearchResult(
        String productId,
        String productName,
        String status,
        String slug,
        String categoryName,
        String categoryId
) {
}
