package com.region.application.model.read;

import java.util.Set;

public record RegionView(
        String id,
        String name,
        String currencyCode,
        String status,
        Set<String> countryCodes
) {
    public boolean active() {
        return "ACTIVE".equals(status);
    }
}
