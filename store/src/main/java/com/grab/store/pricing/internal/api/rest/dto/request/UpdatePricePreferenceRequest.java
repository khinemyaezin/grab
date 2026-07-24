package com.grab.store.pricing.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePricePreferenceRequest(
        @NotBlank String attribute,
        String value,
        boolean taxInclusive
) {
}
