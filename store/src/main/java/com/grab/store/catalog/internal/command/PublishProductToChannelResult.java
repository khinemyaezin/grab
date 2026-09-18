package com.grab.store.catalog.internal.command;

public record PublishProductToChannelResult(
        String productId,
        String variantId,
        String salesChannelId,
        boolean written
) {
}
