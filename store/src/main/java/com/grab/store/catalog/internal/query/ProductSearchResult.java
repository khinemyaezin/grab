package com.grab.store.catalog.internal.query;

public record ProductSearchResult(
        String productId,
        String productName,
        String status,
        String slug,
        String categoryName,
        String categoryId,
        Media thumbnail
) {
    public record Media(
            String id,
            String storageKey,
            String url,
            String contentType,
            int rank
    ) {}
}
