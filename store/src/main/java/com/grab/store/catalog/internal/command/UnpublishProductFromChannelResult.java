package com.grab.store.catalog.internal.command;

public record UnpublishProductFromChannelResult(
        String productId,
        String variantId,
        String salesChannelId,
        boolean deleted
) {
}
