package com.catalog.application.command;

public record PublishProductToChannelResult(
        String productId,
        String variantId,
        String salesChannelId,
        boolean written
) {
}
