package com.grab.store.catalog.internal.command;

public record UpdateVariantResult(
        String productId,
        String variantId,
        String sku,
        String status
) {}
