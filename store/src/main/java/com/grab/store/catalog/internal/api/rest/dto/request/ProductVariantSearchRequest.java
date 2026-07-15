package com.grab.store.catalog.internal.api.rest.dto.request;

public record ProductVariantSearchRequest(
        String query,
        String variantStatus,
        String categoryId,
        String productStatus
) {
}
