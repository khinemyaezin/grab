package com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response;

public record RestoreVariantResponse(
        String productId,
        String variantId,
        String status
) {}
