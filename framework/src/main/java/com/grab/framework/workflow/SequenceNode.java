package com.grab.framework.workflow;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record SequenceNode(String id, List<WorkflowNode> children) implements WorkflowNode {

    public SequenceNode {
        id = requireText(id, "id");
        children = List.copyOf(Objects.requireNonNull(children, "children"));
        if (children.isEmpty()) {
            throw new IllegalArgumentException("sequence must contain at least one child");
        }
        for (int index = 1; index < children.size(); index++) {
            WorkflowNode previous = children.get(index - 1);
            WorkflowNode current = children.get(index);
            if (!current.inputType().isAssignableFrom(previous.outputType())) {
                throw new IllegalArgumentException(
                        "sequence child '" + current.id() + "' cannot accept output from '" + previous.id() + "'"
                );
            }
        }
    }

    @Override
    public WorkflowNodeKind kind() {
        return WorkflowNodeKind.SEQUENCE;
    }

    @Override
    public Class<?> inputType() {
        return children.getFirst().inputType();
    }

    @Override
    public Class<?> outputType() {
        return children.getLast().outputType();
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
