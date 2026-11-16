package com.grab.framework.workflow;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record ChoiceNode<I>(
        String id,
        Class<I> inputType,
        Map<String, WorkflowNode> branches,
        String defaultBranch,
        ChoiceSelector<? super I> selector
) implements WorkflowNode {

    public ChoiceNode {
        id = requireText(id, "id");
        inputType = Objects.requireNonNull(inputType, "inputType");
        Objects.requireNonNull(branches, "branches");
        if (branches.isEmpty()) {
            throw new IllegalArgumentException("choice must contain at least one branch");
        }
        Map<String, WorkflowNode> copy = new LinkedHashMap<>();
        branches.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("choice labels must not be blank");
            }
            copy.put(key, Objects.requireNonNull(value, "choice branch"));
        });
        branches = Collections.unmodifiableMap(copy);
        if (defaultBranch != null && !branches.containsKey(defaultBranch)) {
            throw new IllegalArgumentException("defaultBranch must reference a choice branch");
        }
        Class<?> choiceInputType = inputType;
        if (branches.values().stream().anyMatch(node -> !node.inputType().isAssignableFrom(choiceInputType))) {
            throw new IllegalArgumentException("choice branches must accept the choice input type");
        }
        Class<?> branchOutput = branches.values().iterator().next().outputType();
        if (branches.values().stream().anyMatch(node -> !branchOutput.equals(node.outputType()))) {
            throw new IllegalArgumentException("choice branches must have the same output type");
        }
    }

    public ChoiceNode(String id, Class<I> inputType, Map<String, WorkflowNode> branches, String defaultBranch) {
        this(id, inputType, branches, defaultBranch, null);
    }

    public String selectResolvedInput(Object input) {
        I typedInput = inputType.cast(input);
        return selector == null ? null : selector.select(typedInput);
    }

    @Override
    public WorkflowNodeKind kind() {
        return WorkflowNodeKind.CHOICE;
    }

    @Override
    public Class<?> outputType() {
        return branches.values().iterator().next().outputType();
    }

    @Override
    public List<WorkflowNode> children() {
        return List.copyOf(branches.values());
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
