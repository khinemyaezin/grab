package com.catalog.application.model.write;

public record RestoreVariantResult(
        String productId,
        String variantId,
        String status
) {}
