package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request;

import com.inventory.domain.enums.LocationType;

public record SearchLocationRequest(
        String query,
        LocationType type,
        Boolean active
) {
}
