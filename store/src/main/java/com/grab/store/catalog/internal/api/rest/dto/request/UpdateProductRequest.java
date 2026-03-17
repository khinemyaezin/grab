package com.grab.store.catalog.internal.api.rest.dto.request;

public record UpdateProductRequest(
        String name,
        String categoryId,
        String sellerId,
        String sellerType,
        String condition,
        Boolean offerEligible,
        String slug,
        Boolean featured,
        String moderationNote
) {}
