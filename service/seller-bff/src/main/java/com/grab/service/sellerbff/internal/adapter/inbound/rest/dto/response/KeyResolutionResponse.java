package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

public record KeyResolutionResponse(
        String salesChannelId,
        boolean channelEnabled,
        boolean keyActive
) {}

