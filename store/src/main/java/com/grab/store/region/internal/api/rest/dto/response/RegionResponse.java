package com.grab.store.region.internal.api.rest.dto.response;

import java.util.Set;

public record RegionResponse(
        String regionId,
        String name,
        String currencyCode,
        String status,
        Set<String> countryCodes
) {
}
