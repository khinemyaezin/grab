package com.catalog.application.command;

public record UpdateVariantResult(
        String productId,
        String variantId,
        String sku,
        String status
) {}
