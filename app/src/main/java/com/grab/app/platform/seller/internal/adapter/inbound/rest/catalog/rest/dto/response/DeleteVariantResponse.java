package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

public record DeleteVariantResponse(
        String productId,
        String variantId,
        boolean deleted
) {}
