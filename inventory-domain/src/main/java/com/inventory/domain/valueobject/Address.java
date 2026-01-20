package com.inventory.domain.valueobject;

import java.util.Objects;

public record Address(
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country
) {

    public Address {
        Objects.requireNonNull(country, "country is required");
    }

    public static Address of(String line1, String city, String state, String postalCode, String country) {
        return new Address(line1, null, city, state, postalCode, country);
    }

    public String fullAddress() {
        StringBuilder sb = new StringBuilder();
        if (line1 != null) sb.append(line1);
        if (line2 != null) sb.append(", ").append(line2);
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(", ").append(state);
        if (postalCode != null) sb.append(" ").append(postalCode);
        if (country != null) sb.append(", ").append(country);
        return sb.toString();
    }
}
