package com.grab.outbox.infrastructure;

import com.grab.framework.outbox.OutboxRetryPolicy;
import org.springframework.core.env.PropertyResolver;

import java.time.Duration;

/**
 * Tuning for one module's outbox processor, bound from {@code <prefix>.*} properties, e.g. {@code catalog.outbox}.
 */
public record OutboxProcessorSettings(
        int batchSize,
        Duration retryDelay,
        Duration maxBackoff,
        int maxAttempts,
        Duration claimTimeout,
        Duration retention,
        int cleanupBatchSize,
        Duration cleanupLockAtMostFor,
        Duration cleanupLockAtLeastFor
) {

    public static final int DEFAULT_BATCH_SIZE = 20;
    public static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(30);
    public static final Duration DEFAULT_CLAIM_TIMEOUT = Duration.ofMinutes(2);
    public static final Duration DEFAULT_RETENTION = Duration.ofDays(7);
    public static final int DEFAULT_CLEANUP_BATCH_SIZE = 5_000;
    public static final Duration DEFAULT_CLEANUP_LOCK_AT_MOST_FOR = Duration.ofMinutes(10);
    public static final Duration DEFAULT_CLEANUP_LOCK_AT_LEAST_FOR = Duration.ofMinutes(4);

    public OutboxProcessorSettings {
        batchSize = Math.max(1, batchSize);
        retryDelay = positiveOr(retryDelay, DEFAULT_RETRY_DELAY);
        maxBackoff = positiveOr(maxBackoff, OutboxRetryPolicy.DEFAULT_MAX_DELAY);
        maxAttempts = Math.max(1, maxAttempts);
        claimTimeout = positiveOr(claimTimeout, DEFAULT_CLAIM_TIMEOUT);
        retention = positiveOr(retention, DEFAULT_RETENTION);
        cleanupBatchSize = Math.max(1, cleanupBatchSize);
        cleanupLockAtMostFor = positiveOr(cleanupLockAtMostFor, DEFAULT_CLEANUP_LOCK_AT_MOST_FOR);
        cleanupLockAtLeastFor = cleanupLockAtLeastFor == null || cleanupLockAtLeastFor.isNegative()
                ? Duration.ZERO
                : cleanupLockAtLeastFor;
        if (cleanupLockAtLeastFor.compareTo(cleanupLockAtMostFor) > 0) {
            cleanupLockAtLeastFor = cleanupLockAtMostFor;
        }
    }

    public static OutboxProcessorSettings defaults() {
        return new OutboxProcessorSettings(
                DEFAULT_BATCH_SIZE,
                DEFAULT_RETRY_DELAY,
                OutboxRetryPolicy.DEFAULT_MAX_DELAY,
                OutboxRetryPolicy.DEFAULT_MAX_ATTEMPTS,
                DEFAULT_CLAIM_TIMEOUT,
                DEFAULT_RETENTION,
                DEFAULT_CLEANUP_BATCH_SIZE,
                DEFAULT_CLEANUP_LOCK_AT_MOST_FOR,
                DEFAULT_CLEANUP_LOCK_AT_LEAST_FOR
        );
    }

    /**
     * Binds {@code <prefix>.batch-size}, {@code retry-delay-ms}, {@code max-backoff-ms}, {@code max-attempts},
     * {@code claim-timeout-ms}, {@code retention-ms}, {@code cleanup-batch-size},
     * {@code cleanup-lock-at-most-for-ms} and {@code cleanup-lock-at-least-for-ms}.
     */
    public static OutboxProcessorSettings fromProperties(PropertyResolver properties, String prefix) {
        OutboxProcessorSettings defaults = defaults();
        return new OutboxProcessorSettings(
                properties.getProperty(prefix + ".batch-size", Integer.class, defaults.batchSize()),
                millis(properties, prefix + ".retry-delay-ms", defaults.retryDelay()),
                millis(properties, prefix + ".max-backoff-ms", defaults.maxBackoff()),
                properties.getProperty(prefix + ".max-attempts", Integer.class, defaults.maxAttempts()),
                millis(properties, prefix + ".claim-timeout-ms", defaults.claimTimeout()),
                millis(properties, prefix + ".retention-ms", defaults.retention()),
                properties.getProperty(prefix + ".cleanup-batch-size", Integer.class, defaults.cleanupBatchSize()),
                millis(properties, prefix + ".cleanup-lock-at-most-for-ms", defaults.cleanupLockAtMostFor()),
                millis(properties, prefix + ".cleanup-lock-at-least-for-ms", defaults.cleanupLockAtLeastFor())
        );
    }

    public OutboxRetryPolicy retryPolicy() {
        return new OutboxRetryPolicy(retryDelay, maxBackoff, maxAttempts);
    }

    private static Duration millis(PropertyResolver properties, String key, Duration fallback) {
        Long value = properties.getProperty(key, Long.class, fallback.toMillis());
        return Duration.ofMillis(value);
    }

    private static Duration positiveOr(Duration value, Duration fallback) {
        return value == null || value.isNegative() || value.isZero() ? fallback : value;
    }
}
