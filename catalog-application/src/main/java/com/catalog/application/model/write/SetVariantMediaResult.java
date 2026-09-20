package com.catalog.application.model.write;

import java.util.List;

public record SetVariantMediaResult(
        String productId,
        String variantId,
        List<String> mediaIds,
        String thumbnailMediaId
) {
}
