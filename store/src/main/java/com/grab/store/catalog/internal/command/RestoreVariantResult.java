package com.grab.store.catalog.internal.command;

public record RestoreVariantResult(
        String productId,
        String variantId,
        String status
) {}
