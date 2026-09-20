package com.pricing.application.model.write;

public record PricePreferenceResult(
        String id,
        String attribute,
        String value,
        boolean taxInclusive
) {
}
