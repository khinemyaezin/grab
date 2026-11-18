package com.grab.framework.workflow;

import java.time.Instant;

public record NodeAttempt(String logicalOperationId, String id, int number,
                          Instant deadline, Instant retryAt, int generation) {

    public NodeAttempt {
        if (number < 0 || generation < 0) {
            throw new IllegalArgumentException("Attempt number and generation must not be negative");
        }
    }

    static NodeAttempt initial() {
        return new NodeAttempt(null, null, 0, null, null, 0);
    }

    NodeAttempt dispatch(String operationId, String nextId, Instant dueAt) {
        return new NodeAttempt(operationId, nextId, number + 1, dueAt, null, generation + 1);
    }

    NodeAttempt scheduleRetry(Instant dueAt) {
        return new NodeAttempt(logicalOperationId, id, number, deadline, dueAt, generation + 1);
    }

    NodeAttempt clearRetry() {
        return new NodeAttempt(logicalOperationId, id, number, deadline, null, generation);
    }
}
