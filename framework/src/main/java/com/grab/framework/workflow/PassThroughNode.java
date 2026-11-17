package com.grab.framework.workflow;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record PassThroughNode<T>(String id, Class<T> type) implements WorkflowNode {

    public PassThroughNode {
        id = Objects.requireNonNull(id, "id");
        type = Objects.requireNonNull(type, "type");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    @Override
    public WorkflowNodeKind kind() {
        return WorkflowNodeKind.PASS_THROUGH;
    }

    @Override
    public Class<?> inputType() {
        return type;
    }

    @Override
    public Class<?> outputType() {
        return type;
    }

    @Override
    public List<WorkflowNode> children() {
        return List.of();
    }

    @Override
    public Set<String> referencedNodeIds() {
        return Set.of();
    }
}
