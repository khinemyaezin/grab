package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

public record SalesChannelResponse(
        String salesChannelId,
        String name,
        String type,
        String owner,
        String merchantId,
        String status
) {}

