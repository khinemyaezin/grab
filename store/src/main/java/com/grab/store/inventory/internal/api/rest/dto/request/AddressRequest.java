package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        @NotBlank String country
) {
}
