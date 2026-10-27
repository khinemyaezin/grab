package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

public record ProductModerationResponse(
        String productId,
        String action,
        String oldStatus,
        String newStatus,
        String reason
) {
}
