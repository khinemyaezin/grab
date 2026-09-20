package com.catalog.application.command;

public record UnpublishProductFromChannelResult(
        String productId,
        String variantId,
        String salesChannelId,
        boolean deleted
) {
}
