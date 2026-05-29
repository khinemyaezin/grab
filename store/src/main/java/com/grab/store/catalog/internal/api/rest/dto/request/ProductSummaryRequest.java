package com.grab.store.catalog.internal.api.rest.dto.request;

public record ProductSummaryRequest(
        String productName,
        String sku,
        String variantStatus,
        String categoryId,
        String productStatus,
        int page,
        int size
) {
}
