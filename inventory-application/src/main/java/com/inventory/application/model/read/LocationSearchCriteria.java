package com.inventory.application.model.read;

import com.inventory.domain.enums.LocationType;

public record LocationSearchCriteria(
        String merchantId,
        String query,
        LocationType type,
        Boolean active
) {
}
