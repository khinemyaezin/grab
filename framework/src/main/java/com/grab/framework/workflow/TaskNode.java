package com.grab.framework.workflow;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record TaskNode<I, O>(
        String id,
        Class<I> inputType,
        Class<O> outputType,
        String operation,
        Duration timeout,
        RetryPolicy retryPolicy,
        Optional<CompensationDefinition> compensation,
        TaskEffect effect
) implements WorkflowNode {

    public TaskNode {
        id = requireText(id, "id");
        inputType = Objects.requireNonNull(inputType, "inputType");
        outputType = Objects.requireNonNull(outputType, "outputType");
        operation = requireText(operation, "operation");
        timeout = requirePositive(timeout, "timeout");
        retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
        compensation = compensation == null ? Optional.empty() : compensation;
        effect = Objects.requireNonNull(effect, "effect");
        if (effect == TaskEffect.READ_ONLY && compensation.isPresent()) {
            throw new IllegalArgumentException("read-only tasks cannot declare compensation");
        }
    }

    public TaskNode(
            String id,
            Class<I> inputType,
            Class<O> outputType,
            String operation
    ) {
        this(id, inputType, outputType, operation, Duration.ofMinutes(10), RetryPolicy.DEFAULT, Optional.empty(), TaskEffect.MUTATING);
    }

    public TaskNode(String id, Class<I> inputType, Class<O> outputType, String operation,
                    Duration timeout, RetryPolicy retryPolicy, Optional<CompensationDefinition> compensation) {
        this(id, inputType, outputType, operation, timeout, retryPolicy, compensation, TaskEffect.MUTATING);
    }

    public TaskNode<I, O> withCompensation(CompensationDefinition definition) {
        return new TaskNode<>(id, inputType, outputType, operation, timeout, retryPolicy, Optional.of(definition), effect);
    }

    public TaskNode<I, O> readOnly() {
        return new TaskNode<>(id, inputType, outputType, operation, timeout, retryPolicy, compensation, TaskEffect.READ_ONLY);
    }

    @Override
    public WorkflowNodeKind kind() {
        return WorkflowNodeKind.TASK;
    }

    @Override
    public List<WorkflowNode> children() {
        return List.of();
    }

    @Override
    public Set<String> referencedNodeIds() {
        return compensation.map(value -> Set.copyOf(value.dependsOn())).orElseGet(Set::of);
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    private static Duration requirePositive(Duration value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isNegative() || value.isZero()) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
