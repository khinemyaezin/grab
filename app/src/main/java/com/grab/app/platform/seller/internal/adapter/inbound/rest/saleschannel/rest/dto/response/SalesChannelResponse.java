package com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.response;

public record SalesChannelResponse(
        String salesChannelId,
        String name,
        String type,
        String owner,
        String merchantId,
        String status
) {
}
