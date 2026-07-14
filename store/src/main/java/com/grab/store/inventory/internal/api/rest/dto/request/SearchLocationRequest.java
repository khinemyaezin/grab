package com.grab.store.inventory.internal.api.rest.dto.request;

import com.inventory.domain.enums.LocationType;

public record SearchLocationRequest(
        String query,
        LocationType type,
        Boolean active
) {
}
