package com.grab.store.catalog.internal.command;

public record DeleteVariantResult(
        String productId,
        String variantId,
        boolean deleted
) {}
