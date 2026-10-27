package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePricePreferenceRequest(
        @NotBlank String attribute,
        String value,
        boolean taxInclusive
) {
}
