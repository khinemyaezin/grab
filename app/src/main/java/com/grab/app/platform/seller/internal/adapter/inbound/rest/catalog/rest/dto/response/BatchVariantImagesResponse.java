package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

import java.util.List;

public record BatchVariantImagesResponse(
        String productId,
        String variantId,
        List<String> mediaIds,
        String thumbnailMediaId
) {
}
