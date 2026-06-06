package com.inventory.infrastructure.view;

import com.inventory.domain.enums.ZoneType;

public record ZoneView(
        String uuid,
        String code,
        String name,
        ZoneType type,
        boolean active,
        String locationId
) {
}
