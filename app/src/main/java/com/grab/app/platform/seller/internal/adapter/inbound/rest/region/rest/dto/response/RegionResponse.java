package com.grab.app.platform.seller.internal.adapter.inbound.rest.region.rest.dto.response;

import java.util.Set;

public record RegionResponse(
        String regionId,
        String name,
        String currencyCode,
        String status,
        Set<String> countryCodes
) {
}
