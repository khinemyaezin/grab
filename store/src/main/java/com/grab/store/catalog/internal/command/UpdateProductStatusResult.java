package com.grab.store.catalog.internal.command;

public record UpdateProductStatusResult(
        String productId,
        String oldStatus,
        String newStatus
) {}
