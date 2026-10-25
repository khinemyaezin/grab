package com.grab.app.productmerchandising.inventory.internal.api.rest.dto.request;

import com.inventory.domain.enums.ZoneType;

public record UpdateZoneRequest(
        String code,
        String name,
        ZoneType type,
        Boolean active
) {
}
