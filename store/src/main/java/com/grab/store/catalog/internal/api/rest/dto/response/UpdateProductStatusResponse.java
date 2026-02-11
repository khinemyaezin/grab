package com.grab.store.catalog.internal.api.rest.dto.response;

public record UpdateProductStatusResponse(
        String productId,
        String oldStatus,
        String newStatus
) {}
