package com.grab.store.catalog.internal.api.rest.dto.response;

public record ProductSearchResponse(
        String productId,
        String productName,
        String status,
        String slug,
        String categoryName,
        String categoryId
) {
}
