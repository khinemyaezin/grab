package com.region.application.model.read;

import java.util.Set;

public record RegionResult(
        String regionId,
        String name,
        String currencyCode,
        String status,
        Set<String> countryCodes
) {
}
