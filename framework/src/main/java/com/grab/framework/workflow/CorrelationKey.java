package com.grab.framework.workflow;

import java.util.Objects;

public record CorrelationKey(String value) {

    public CorrelationKey {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("correlation key must not be blank");
        }
    }

    public static CorrelationKey of(String value) {
        return new CorrelationKey(value);
    }

    public static CorrelationKey product(String productId) {
        Objects.requireNonNull(productId, "productId");
        return of("product:" + productId);
    }
}
