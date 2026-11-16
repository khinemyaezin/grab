package com.grab.framework.workflow;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record ParallelNode(String id, List<WorkflowNode> branches) implements WorkflowNode {

    public ParallelNode {
        id = requireText(id, "id");
        branches = List.copyOf(Objects.requireNonNull(branches, "branches"));
        if (branches.isEmpty()) {
            throw new IllegalArgumentException("parallel must contain at least one branch");
        }
        Class<?> branchInput = branches.getFirst().inputType();
        if (branches.stream().anyMatch(branch -> !branch.inputType().isAssignableFrom(branchInput))) {
            throw new IllegalArgumentException("parallel branches must accept the same input type");
        }
    }

    @Override
    public WorkflowNodeKind kind() {
        return WorkflowNodeKind.PARALLEL;
    }

    @Override
    public Class<?> inputType() {
        return branches.getFirst().inputType();
    }

    @Override
    public Class<?> outputType() {
        return List.class;
    }

    @Override
    public List<WorkflowNode> children() {
        return branches;
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
