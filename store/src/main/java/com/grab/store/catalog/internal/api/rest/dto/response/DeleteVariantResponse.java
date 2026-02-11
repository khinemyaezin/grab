package com.grab.store.catalog.internal.api.rest.dto.response;

public record DeleteVariantResponse(
        String productId,
        String variantId,
        boolean deleted
) {}
