package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateFacilityLocationRequest(
        @NotBlank(message = "Location code is required")
        String code,
        @NotBlank(message = "Location name is required")
        String name,
        String type,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country
) {}
