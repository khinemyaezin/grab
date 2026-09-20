package com.catalog.application.readmodel;

public record ProductPublicationView(
        String productId,
        String variantId,
        String salesChannelId
) {
}
