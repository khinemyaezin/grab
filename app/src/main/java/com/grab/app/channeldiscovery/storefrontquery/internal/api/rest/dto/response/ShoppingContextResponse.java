package com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.dto.response;

public record ShoppingContextResponse(
        String salesChannelId,
        String regionId,
        String countryCode,
        String currencyCode,
        String locale,
        boolean regionServed
) {
}
