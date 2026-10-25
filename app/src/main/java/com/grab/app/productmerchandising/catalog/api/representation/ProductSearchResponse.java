package com.grab.app.productmerchandising.catalog.api.representation;

import java.util.List;

public record ProductSearchResponse(
        String productId,
        String productName,
        String status,
        String slug,
        String categoryName,
        String categoryId,
        Media thumbnail,
        List<Publication> publications
) {
    public ProductSearchResponse {
        publications = publications == null ? List.of() : List.copyOf(publications);
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
