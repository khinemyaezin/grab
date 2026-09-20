package com.catalog.application.model.write;

public record UpdateVariantResult(
        String productId,
        String variantId,
        String sku,
        String status
) {}
