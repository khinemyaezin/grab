package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

import java.io.Serializable;
import java.util.List;

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
        List<GetProductResponse.Description> descriptions,
        List<GetProductResponse.Media> medias,
        String moderationNote
) implements Serializable {
}
