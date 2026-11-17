package com.grab.framework.workflow;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public record LocalMapNode<I, O>(String id, Class<I> inputType, Class<O> outputType,
                                 Function<? super I, ? extends O> mapper) implements WorkflowNode {

    public LocalMapNode {
        Objects.requireNonNull(mapper, "mapper");
    }

    public Object apply(Object input) {
        I typed = inputType.cast(input);
        O result = mapper.apply(typed);
        return outputType.cast(Objects.requireNonNull(result, "Local map returned null"));
    }

    @Override
    public WorkflowNodeKind kind() {
        return WorkflowNodeKind.LOCAL_MAP;
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
