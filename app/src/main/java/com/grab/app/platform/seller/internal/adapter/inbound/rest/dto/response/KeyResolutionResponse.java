package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

public record KeyResolutionResponse(
        String salesChannelId,
        boolean channelEnabled,
        boolean keyActive
) {}

