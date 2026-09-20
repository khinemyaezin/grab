package com.catalog.application.readmodel;

public record ProductHeroMediaView(
        String productId,
        String mediaId,
        String storageKey,
        String contentType,
        int rank
) {
}
