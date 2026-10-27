package com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.dto.response;

public record ShoppingContextResponse(
        String salesChannelId,
        String regionId,
        String countryCode,
        String currencyCode,
        String locale,
        boolean regionServed
) {
}
