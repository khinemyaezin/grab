package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateFacilityBinRequest(
        @NotBlank(message = "Bin code is required")
        String code,
        @NotBlank(message = "Bin name is required")
        String name,
        Integer maxCapacity
) {}
