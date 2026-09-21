package com.catalog.application.model.write;

public record DeleteVariantResult(
        String productId,
        String variantId,
        boolean deleted
) {}
