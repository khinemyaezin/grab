package com.grab.framework.workflow;

import java.time.Duration;
import java.util.Objects;

public record RetryPolicy(int maxAttempts, Duration initialDelay, double multiplier, Duration maxDelay) {

    public static final RetryPolicy DEFAULT = new RetryPolicy(
            3,
            Duration.ofSeconds(1),
            2.0,
            Duration.ofSeconds(30)
    );

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least one");
        }
        initialDelay = Objects.requireNonNull(initialDelay, "initialDelay");
        maxDelay = Objects.requireNonNull(maxDelay, "maxDelay");
        if (initialDelay.isNegative() || initialDelay.isZero()) {
            throw new IllegalArgumentException("initialDelay must be positive");
        }
        if (maxDelay.compareTo(initialDelay) < 0) {
            throw new IllegalArgumentException("maxDelay must be at least initialDelay");
        }
        if (!Double.isFinite(multiplier) || multiplier < 1.0) {
            throw new IllegalArgumentException("multiplier must be finite and at least one");
        }
    }

    public Duration delayForAttempt(int attempt) {
        if (attempt < 1) {
            throw new IllegalArgumentException("attempt must be at least one");
        }
        double exponent = Math.min(attempt - 1, 30);
        long delayMillis = (long) Math.min(
                maxDelay.toMillis(),
                initialDelay.toMillis() * Math.pow(multiplier, exponent)
        );
        return Duration.ofMillis(Math.max(delayMillis, initialDelay.toMillis()));
    }
}
