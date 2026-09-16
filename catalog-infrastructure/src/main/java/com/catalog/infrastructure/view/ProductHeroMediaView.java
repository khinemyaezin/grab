package com.catalog.infrastructure.view;

public record ProductHeroMediaView(
        String productId,
        String mediaId,
        String storageKey,
        String contentType,
        int rank
) {
}
