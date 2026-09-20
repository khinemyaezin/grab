package com.catalog.application.command;

public record RestoreVariantResult(
        String productId,
        String variantId,
        String status
) {}
