package com.grab.store.catalog.internal.command;

public record UpdateProductResult(
        String productId,
        String name,
        String categoryId,
        String sellerId,
        String sellerType,
        String condition,
        boolean offerEligible,
        String status,
        String slug,
        boolean featured,
        java.util.List<GetProductPayload.Description> descriptions,
        java.util.List<GetProductPayload.Media> medias,
        String moderationNote
) {}
