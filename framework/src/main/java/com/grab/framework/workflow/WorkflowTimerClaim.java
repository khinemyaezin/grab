package com.grab.framework.workflow;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record WorkflowTimerClaim(
        long timerId,
        String executionId,
        WorkflowTimer timer,
        String claimToken,
        Instant leaseExpiresAt
) implements Serializable {

    public WorkflowTimerClaim {
        if (timerId < 1) {
            throw new IllegalArgumentException("timerId must be positive");
        }
        executionId = requireText(executionId, "executionId");
        timer = Objects.requireNonNull(timer, "timer");
        claimToken = requireText(claimToken, "claimToken");
        leaseExpiresAt = Objects.requireNonNull(leaseExpiresAt, "leaseExpiresAt");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
