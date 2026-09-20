package com.catalog.application.model.write;

public record ModerateProductResult(
        String productId,
        String action,
        String oldStatus,
        String newStatus,
        String reason
) {
}
