package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request;

import com.inventory.domain.enums.ZoneType;

public record UpdateZoneRequest(
        String code,
        String name,
        ZoneType type,
        Boolean active
) {
}
