package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record CreateFacilityLocationRequest(
        @NotBlank(message = "Location code is required")
        String code,
        @NotBlank(message = "Location name is required")
        String name,
        String type,
        @Valid AddressRequest address,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country
) {
    public String resolveLine1() {
        return address != null && address.line1() != null ? address.line1() : line1;
    }

    public String resolveLine2() {
        return address != null && address.line2() != null ? address.line2() : line2;
    }

    public String resolveCity() {
        return address != null && address.city() != null ? address.city() : city;
    }

    public String resolveState() {
        return address != null && address.state() != null ? address.state() : state;
    }

    public String resolvePostalCode() {
        return address != null && address.postalCode() != null ? address.postalCode() : postalCode;
    }

    public String resolveCountry() {
        return address != null && address.country() != null ? address.country() : country;
    }

    public record AddressRequest(
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            @NotBlank String country
    ) {}
}
