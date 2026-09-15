package com.grab.store.catalog.internal.command;

import java.util.List;

public record SetVariantMediaResult(
        String productId,
        String variantId,
        List<String> mediaIds,
        String thumbnailMediaId
) {
}
