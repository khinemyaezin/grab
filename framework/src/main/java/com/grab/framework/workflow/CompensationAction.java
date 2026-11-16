package com.grab.framework.workflow;

import java.time.Duration;
import java.util.Objects;

public record CompensationAction(
        String nodeId,
        String operation,
        Duration timeout,
        RetryPolicy retryPolicy
) {

    public CompensationAction {
        nodeId = requireText(nodeId, "nodeId");
        operation = requireText(operation, "operation");
        timeout = Objects.requireNonNull(timeout, "timeout");
        retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
