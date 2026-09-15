package com.grab.store.catalog.internal.api.rest.dto.response;

import java.util.List;

public record BatchVariantImagesResponse(
        String productId,
        String variantId,
        List<String> mediaIds,
        String thumbnailMediaId
) {
}
