package com.grab.framework.workflow;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record WaitForSignalNode(
        String id,
        Class<?> inputType,
        String signalType,
        Class<?> payloadType,
        Duration timeout
) implements WorkflowNode {

    public WaitForSignalNode {
        id = requireText(id, "id");
        inputType = Objects.requireNonNull(inputType, "inputType");
        signalType = requireText(signalType, "signalType");
        payloadType = Objects.requireNonNull(payloadType, "payloadType");
        timeout = Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }

    @Override
    public WorkflowNodeKind kind() {
        return WorkflowNodeKind.WAIT_FOR_SIGNAL;
    }

    @Override
    public Class<?> outputType() {
        return payloadType;
    }

    @Override
    public List<WorkflowNode> children() {
        return List.of();
    }

    @Override
    public Set<String> referencedNodeIds() {
        return Set.of();
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
