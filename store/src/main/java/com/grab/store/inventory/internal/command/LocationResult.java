package com.grab.store.inventory.internal.command;

public record LocationResult(
        String id,
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
