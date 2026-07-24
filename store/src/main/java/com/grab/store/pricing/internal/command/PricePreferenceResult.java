package com.grab.store.pricing.internal.command;

public record PricePreferenceResult(
        String id,
        String attribute,
        String value,
        boolean taxInclusive
) {
}
