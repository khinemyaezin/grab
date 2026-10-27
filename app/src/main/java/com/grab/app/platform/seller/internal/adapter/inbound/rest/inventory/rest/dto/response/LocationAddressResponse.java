package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

public record LocationAddressResponse(
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country
) {
}
