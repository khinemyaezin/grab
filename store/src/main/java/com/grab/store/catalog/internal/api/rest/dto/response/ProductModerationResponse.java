package com.grab.store.catalog.internal.api.rest.dto.response;

public record ProductModerationResponse(
        String productId,
        String action,
        String oldStatus,
        String newStatus,
        String reason
) {
}
