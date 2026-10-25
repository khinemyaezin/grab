package com.grab.app.channeldiscovery.saleschannel.internal.api.rest.dto.response;

public record SalesChannelResponse(
        String salesChannelId,
        String name,
        String type,
        String owner,
        String merchantId,
        String status
) {
}
