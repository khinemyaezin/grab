package com.catalog.application.model.write;

public record UnpublishProductFromChannelResult(
        String productId,
        String variantId,
        String salesChannelId,
        boolean deleted
) {
}
