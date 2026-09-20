package com.inventory.application.model.read;

import com.inventory.domain.enums.ZoneType;

public record ZoneSearchCriteria(
        String merchantId,
        String locationId,
        String query,
        ZoneType type,
        Boolean active
) {
}
