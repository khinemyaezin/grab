package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

public record RestoreVariantResponse(
        String productId,
        String variantId,
        String status
) {}
