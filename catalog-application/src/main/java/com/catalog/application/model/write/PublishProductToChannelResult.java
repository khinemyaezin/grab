package com.catalog.application.model.write;

public record PublishProductToChannelResult(
        String productId,
        String variantId,
        String salesChannelId,
        boolean written
) {
}
