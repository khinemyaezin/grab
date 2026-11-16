package com.grab.app.workflow.internal.config;

import com.grab.framework.workflow.WorkflowConcurrencyException;
import com.grab.framework.workflow.WorkflowReceiptUniquenessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Retries only database races that are safe to replay in a fresh transaction.
 * The retry advice is ordered outside workflow transaction advice.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
        retryFor = {
                WorkflowConcurrencyException.class,
                WorkflowReceiptUniquenessException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 25, multiplier = 2, maxDelay = 200, random = true)
)
public @interface WorkflowConcurrencyRetryable {
}
