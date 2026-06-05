package com.inventory.infrastructure.view;

import com.inventory.domain.enums.LocationType;

public record LocationView(
        String uuid,
        String code,
        String name,
        LocationType type,
        String street,
        String street2,
        String city,
        String state,
        String postalCode,
        String country,
        boolean active
) {
}
