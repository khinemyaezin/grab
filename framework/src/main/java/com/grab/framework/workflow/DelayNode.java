package com.grab.framework.workflow;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record DelayNode(String id, Class<?> inputType, Duration delay) implements WorkflowNode {

    public DelayNode {
        id = requireText(id, "id");
        inputType = Objects.requireNonNull(inputType, "inputType");
        delay = Objects.requireNonNull(delay, "delay");
        if (delay.isNegative() || delay.isZero()) {
            throw new IllegalArgumentException("delay must be positive");
        }
    }

    @Override
    public WorkflowNodeKind kind() {
        return WorkflowNodeKind.DELAY;
    }

    @Override
    public Class<?> outputType() {
        return inputType;
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
