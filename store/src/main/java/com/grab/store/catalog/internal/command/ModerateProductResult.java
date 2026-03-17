package com.grab.store.catalog.internal.command;

public record ModerateProductResult(
        String productId,
        String action,
        String oldStatus,
        String newStatus,
        String reason
) {
}
