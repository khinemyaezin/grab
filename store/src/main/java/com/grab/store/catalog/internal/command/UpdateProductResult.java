package com.grab.store.catalog.internal.command;

public record UpdateProductResult(
        String productId,
        String name,
        String categoryId,
        String status,
        String slug,
        boolean featured
) {}
