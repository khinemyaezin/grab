package com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response;

public record UpdateProductStatusResponse(
        String productId,
        String oldStatus,
        String newStatus
) {}
