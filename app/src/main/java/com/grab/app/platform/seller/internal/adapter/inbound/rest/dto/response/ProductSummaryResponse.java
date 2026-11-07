package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

public record ProductSummaryResponse(
        String productId,
        String productName,
        String status,
        String slug,
        String categoryName,
        String categoryId
) {}
