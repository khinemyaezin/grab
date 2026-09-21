package com.catalog.application.model.read;

public record ProductHeroMediaView(
        String productId,
        String mediaId,
        String storageKey,
        String contentType,
        int rank
) {
}
