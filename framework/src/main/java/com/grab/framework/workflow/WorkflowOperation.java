package com.grab.framework.workflow;

import java.util.Objects;

public record WorkflowOperation<Q, R>(String name, Class<Q> requestType, Class<R> resultType, TaskEffect effect) {

    public WorkflowOperation {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Workflow operation name is required");
        }
        Objects.requireNonNull(requestType, "requestType");
        Objects.requireNonNull(resultType, "resultType");
        Objects.requireNonNull(effect, "effect");
    }

    public static <Q, R> WorkflowOperation<Q, R> mutation(String name, Class<Q> request, Class<R> result) {
        return new WorkflowOperation<>(name, request, result, TaskEffect.MUTATING);
    }

    public static <Q, R> WorkflowOperation<Q, R> assertion(String name, Class<Q> request, Class<R> result) {
        return new WorkflowOperation<>(name, request, result, TaskEffect.READ_ONLY);
    }
}
