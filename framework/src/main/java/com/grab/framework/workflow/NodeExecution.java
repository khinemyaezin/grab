package com.grab.framework.workflow;

import java.time.Instant;
import java.util.Objects;

public record NodeExecution(
        String nodeId,
        NodeExecutionStatus status,
        WorkflowPhase phase,
        NodeAttempt attempt,
        Object input,
        Object output,
        String error,
        String acceptedFingerprint
) {

    public NodeExecution {
        requireText(nodeId, "nodeId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(attempt, "attempt");
        if (status == NodeExecutionStatus.DISPATCHED) {
            requireText(attempt.logicalOperationId(), "logicalOperationId");
            requireText(attempt.id(), "attemptId");
            Objects.requireNonNull(attempt.deadline(), "deadline");
            if (attempt.number() < 1) {
                throw new IllegalArgumentException("Dispatched nodes require a positive attempt");
            }
        }
    }

    public static NodeExecution pending(String nodeId) {
        NodeAttempt attempt = NodeAttempt.initial();
        return new NodeExecution(nodeId, NodeExecutionStatus.PENDING, WorkflowPhase.FORWARD,
                attempt, null, null, null, null);
    }

    public static NodeExecution pendingCompensation(String nodeId, CompensationInput<?, ?> input) {
        NodeAttempt attempt = NodeAttempt.initial();
        return new NodeExecution(nodeId, NodeExecutionStatus.ACTIVE, WorkflowPhase.COMPENSATION,
                attempt, input, null, null, null);
    }

    public NodeExecution active(Object resolvedInput) {
        return new NodeExecution(nodeId, NodeExecutionStatus.ACTIVE, phase, attempt,
                resolvedInput, output, error, acceptedFingerprint);
    }

    public NodeExecution dispatch(String operationId, String nextAttemptId, Object resolvedInput, Instant nextDeadline) {
        NodeAttempt next = attempt.dispatch(operationId, nextAttemptId, nextDeadline);
        return new NodeExecution(nodeId, NodeExecutionStatus.DISPATCHED, phase, next,
                resolvedInput, output, null, acceptedFingerprint);
    }

    public NodeExecution succeed(Object result, String fingerprint) {
        NodeExecutionStatus completed = phase == WorkflowPhase.COMPENSATION
                ? NodeExecutionStatus.COMPENSATED : NodeExecutionStatus.SUCCEEDED;
        return transition(completed, result, null, fingerprint);
    }

    public NodeExecution retryWait(Instant dueAt, String failure, String fingerprint) {
        NodeAttempt next = attempt.scheduleRetry(dueAt);
        return new NodeExecution(nodeId, NodeExecutionStatus.RETRY_WAIT, phase, next,
                input, output, failure, fingerprint);
    }

    public NodeExecution retryPending() {
        return transition(NodeExecutionStatus.PENDING, output, error, null);
    }

    public NodeExecution fail(String failure) {
        return fail(failure, acceptedFingerprint);
    }

    public NodeExecution fail(String failure, String fingerprint) {
        return transition(NodeExecutionStatus.FAILED, output, failure, fingerprint);
    }

    public NodeExecution unknown(String failure) {
        return transition(NodeExecutionStatus.UNKNOWN, output, failure, acceptedFingerprint);
    }

    public NodeExecution skipped() {
        return transition(NodeExecutionStatus.SKIPPED, output, null, acceptedFingerprint);
    }

    public String logicalOperationId() {
        return attempt.logicalOperationId();
    }

    public String attemptId() {
        return attempt.id();
    }

    public int attempts() {
        return attempt.number();
    }

    public Instant deadline() {
        return attempt.deadline();
    }

    public Instant retryAt() {
        return attempt.retryAt();
    }

    public int generation() {
        return attempt.generation();
    }

    private NodeExecution transition(NodeExecutionStatus nextStatus, Object result, String failure, String fingerprint) {
        NodeAttempt next = attempt.clearRetry();
        return new NodeExecution(nodeId, nextStatus, phase, next, input, result, failure, fingerprint);
    }

    private static void requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
