package com.grab.store.catalog.internal.command;

public record UnpublishProductFromChannelResult(
        String productId,
        String salesChannelId,
        boolean deleted
) {
}
