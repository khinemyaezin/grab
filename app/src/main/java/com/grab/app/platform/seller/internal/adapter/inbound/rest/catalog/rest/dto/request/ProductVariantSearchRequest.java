package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request;

public record ProductVariantSearchRequest(
        String query,
        String variantStatus,
        String categoryId,
        String productStatus
) {
}
