package com.inventory.application.model.read;

import com.grab.framework.id.Id;

public record GetLocationResult(
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
