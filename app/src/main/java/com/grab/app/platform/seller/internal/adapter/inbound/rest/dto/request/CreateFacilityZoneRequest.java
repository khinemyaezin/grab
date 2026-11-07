package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateFacilityZoneRequest(
        @NotBlank(message = "Zone code is required")
        String code,
        @NotBlank(message = "Zone name is required")
        String name,
        String type
) {}
