package com.catalog.application.model.read;

public record ProductPublicationView(
        String productId,
        String variantId,
        String salesChannelId
) {
}
