package com.grab.store.inventory.internal.query;

import com.grab.framework.id.Id;

public record SearchLocationsResult(
        Id id,
        String code,
        String name,
        String type,
        boolean active,
        Address address
) {

    public record Address(
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country
    ) {
    }
}
