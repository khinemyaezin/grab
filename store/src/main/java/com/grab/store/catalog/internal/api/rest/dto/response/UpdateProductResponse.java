package com.grab.store.catalog.internal.api.rest.dto.response;

import java.io.Serializable;

public record UpdateProductResponse(
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
        java.util.List<GetProductResponse.Description> descriptions,
        java.util.List<GetProductResponse.Media> medias,
        String moderationNote
) implements Serializable {
}
