package com.catalog.infrastructure.view;

public record ProductPublicationView(
        String productId,
        String variantId,
        String salesChannelId
) {
}
