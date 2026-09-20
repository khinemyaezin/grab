package com.catalog.application.query;

import java.util.List;

public record ProductSearchResult(
        String productId,
        String productName,
        String status,
        String slug,
        String categoryName,
        String categoryId,
        Media thumbnail,
        List<Publication> publications
) {
    public ProductSearchResult(
            String productId,
            String productName,
            String status,
            String slug,
            String categoryName,
            String categoryId,
            Media thumbnail
    ) {
        this(productId, productName, status, slug, categoryName, categoryId, thumbnail, List.of());
    }

    public record Publication(String salesChannelId) {}
    public record Media(
            String id,
            String storageKey,
            String url,
            String contentType,
            int rank
    ) {}
}
