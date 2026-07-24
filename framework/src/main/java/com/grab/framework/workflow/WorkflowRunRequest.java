package com.grab.framework.workflow;

import java.util.Objects;
import java.util.Optional;

public final class WorkflowRunRequest {

    private final String workflowId;
    private final String idempotencyKey;

    private WorkflowRunRequest(String workflowId, String idempotencyKey) {
        this.workflowId = Objects.requireNonNull(workflowId, "workflowId");
        if (workflowId.isBlank()) {
            throw new IllegalArgumentException("workflowId must not be blank");
        }
        if (idempotencyKey != null && idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must not be blank when provided");
        }
        this.idempotencyKey = idempotencyKey;
    }

    public static WorkflowRunRequest of(String workflowId) {
        return new WorkflowRunRequest(workflowId, null);
    }

    public static WorkflowRunRequest of(String workflowId, String idempotencyKey) {
        return new WorkflowRunRequest(workflowId, idempotencyKey);
    }

    public String workflowId() {
        return workflowId;
    }

    public Optional<String> idempotencyKey() {
        return Optional.ofNullable(idempotencyKey);
    }
}
