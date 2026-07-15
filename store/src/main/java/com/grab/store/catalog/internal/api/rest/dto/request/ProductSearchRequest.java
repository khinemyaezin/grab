package com.grab.store.catalog.internal.api.rest.dto.request;

public record ProductSearchRequest(
        String query,
        String variantStatus,
        String categoryId,
        String productStatus
) {
}
