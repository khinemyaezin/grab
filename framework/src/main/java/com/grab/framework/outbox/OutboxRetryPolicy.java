package com.grab.framework.outbox;

import java.time.Duration;
import java.util.Objects;
import java.util.function.DoubleSupplier;

/**
 * Bounded exponential backoff with jitter: {@code baseDelay * 2^(attempt-1)}, +/- {@code jitterRatio},
 * capped at {@code maxDelay}. After {@code maxAttempts} delivery attempts the row is dead-lettered.
 */
public final class OutboxRetryPolicy {

    public static final int DEFAULT_MAX_ATTEMPTS = 10;
    public static final Duration DEFAULT_MAX_DELAY = Duration.ofMinutes(15);
    public static final double DEFAULT_JITTER_RATIO = 0.2d;

    private static final int MAX_SHIFT = 30;

    private final Duration baseDelay;
    private final Duration maxDelay;
    private final int maxAttempts;
    private final double jitterRatio;
    private final DoubleSupplier random;

    public OutboxRetryPolicy(Duration baseDelay, Duration maxDelay, int maxAttempts) {
        this(baseDelay, maxDelay, maxAttempts, DEFAULT_JITTER_RATIO, Math::random);
    }

    public OutboxRetryPolicy(
            Duration baseDelay,
            Duration maxDelay,
            int maxAttempts,
            double jitterRatio,
            DoubleSupplier random
    ) {
        this.baseDelay = requirePositive(baseDelay);
        Duration cap = maxDelay == null ? DEFAULT_MAX_DELAY : maxDelay;
        this.maxDelay = cap.compareTo(this.baseDelay) < 0 ? this.baseDelay : cap;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.jitterRatio = Math.clamp(jitterRatio, 0d, 1d);
        this.random = Objects.requireNonNull(random, "random");
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public boolean isExhausted(int attemptCount) {
        return attemptCount >= maxAttempts;
    }

    /**
     * @param attempt the 1-based number of the attempt that just failed
     */
    public Duration backoff(int attempt) {
        int shift = Math.min(Math.max(attempt, 1) - 1, MAX_SHIFT);
        long baseMillis = baseDelay.toMillis();
        long capMillis = maxDelay.toMillis();
        long exponential = baseMillis > (capMillis >> shift) ? capMillis : baseMillis << shift;
        long uncapped = Math.min(exponential, capMillis);
        double factor = 1d + jitterRatio * (2d * random.getAsDouble() - 1d);
        long jittered = Math.round(uncapped * factor);
        return Duration.ofMillis(Math.clamp(jittered, 0L, capMillis));
    }

    private static Duration requirePositive(Duration value) {
        if (value == null || value.isNegative() || value.isZero()) {
            throw new IllegalArgumentException("baseDelay" + " must be positive");
        }
        return value;
    }
}
