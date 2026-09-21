package com.saleschannel.application.model.read;

public record SalesChannelResult(
        String salesChannelId,
        String name,
        String type,
        String owner,
        String merchantId,
        String status
) {
}
