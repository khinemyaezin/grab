package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePricePreferenceRequest(
        @NotBlank(message = "Attribute name is required")
        String attribute,
        @NotBlank(message = "Value is required")
        String value,
        boolean taxInclusive
) {}
