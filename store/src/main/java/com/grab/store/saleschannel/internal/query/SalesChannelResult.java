package com.grab.store.saleschannel.internal.query;

public record SalesChannelResult(
        String salesChannelId,
        String name,
        String type,
        String owner,
        String merchantId,
        String status
) {
}
