package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

public record UpdateProductStatusResponse(
        String productId,
        String oldStatus,
        String newStatus
) {}
