package com.grab.store.pricing.internal.api.rest.dto.response;

public record PricePreferenceResponse(
        String id,
        String attribute,
        String value,
        boolean taxInclusive
) {
}
