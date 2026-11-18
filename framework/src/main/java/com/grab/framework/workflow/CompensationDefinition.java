package com.grab.framework.workflow;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public record CompensationDefinition(
        String operation,
        List<String> dependsOn,
        Duration timeout,
        RetryPolicy retryPolicy
) {

    public CompensationDefinition {
        operation = requireText(operation, "operation");
        dependsOn = List.copyOf(Objects.requireNonNull(dependsOn, "dependsOn"));
        if (dependsOn.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("dependsOn must contain non-blank node IDs");
        }
        timeout = Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
    }

    public static CompensationDefinition of(String operation, List<String> dependsOn) {
        return new CompensationDefinition(
                operation,
                dependsOn,
                java.time.Duration.ofMinutes(10),
                RetryPolicy.DEFAULT
        );
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
