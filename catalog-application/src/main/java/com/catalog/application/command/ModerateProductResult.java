package com.catalog.application.command;

public record ModerateProductResult(
        String productId,
        String action,
        String oldStatus,
        String newStatus,
        String reason
) {
}
