package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request;

import com.inventory.domain.enums.ZoneType;
import jakarta.validation.constraints.NotBlank;

public record SearchZoneRequest(
        @NotBlank String locationId,
        String query,
        ZoneType type,
        Boolean active
) {
}
