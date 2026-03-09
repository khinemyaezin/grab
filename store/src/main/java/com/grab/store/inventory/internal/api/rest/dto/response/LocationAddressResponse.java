package com.grab.store.inventory.internal.api.rest.dto.response;

public record LocationAddressResponse(
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country
) {
}
