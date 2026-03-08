package com.grab.store.inventory.internal.query;

import java.util.List;

public record GetLocationResult(
        String id,
        String code,
        String name,
        String type,
        boolean active,
        Address address,
        List<Zone> zones
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

    public record Zone(
            String id,
            String code,
            String name,
            String type,
            boolean active,
            List<Bin> bins
    ) {
    }

    public record Bin(
            String id,
            String code,
            String name,
            Integer maxCapacity,
            boolean active
    ) {
    }
}
