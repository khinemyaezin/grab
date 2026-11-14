package com.grab.app.shared.bootstrap;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensure pattern for idempotent "find or create" handlers. When a concurrent creator wins the unique constraint,
 * the handler is re-invoked. Retry advice is ordered outside the transaction advice
 * ({@code @EnableRetry(order = HIGHEST_PRECEDENCE)}), so each attempt runs in a fresh transaction and its find step
 * re-reads the committed row.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
        retryFor = {DataIntegrityViolationException.class, ConstraintViolationException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 50, multiplier = 2, maxDelay = 200, random = true))
public @interface EnsureRetryable {
}
