package com.catalog.application.command;

public record DeleteVariantResult(
        String productId,
        String variantId,
        boolean deleted
) {}
