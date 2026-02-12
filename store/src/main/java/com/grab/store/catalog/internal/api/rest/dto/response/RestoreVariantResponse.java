package com.grab.store.catalog.internal.api.rest.dto.response;

public record RestoreVariantResponse(
        String productId,
        String variantId,
        String status
) {}
