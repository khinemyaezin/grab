package com.grab.framework.workflow;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public final class JoinedParallelNode<I, L, R, O> implements WorkflowNode {

    private final String id;
    private final WorkflowStep<I, L> left;
    private final WorkflowStep<I, R> right;
    private final Class<O> outputType;
    private final BiFunction<? super L, ? super R, ? extends O> join;

    JoinedParallelNode(String id, WorkflowStep<I, L> left, WorkflowStep<I, R> right, Class<O> outputType,
                       BiFunction<? super L, ? super R, ? extends O> join) {
        this.id = id;
        this.left = left;
        this.right = right;
        this.outputType = outputType;
        this.join = Objects.requireNonNull(join, "join");
    }

    public Object join(Object leftResult, Object rightResult) {
        L first = left.outputType().cast(leftResult);
        R second = right.outputType().cast(rightResult);
        O result = join.apply(first, second);
        return outputType.cast(Objects.requireNonNull(result, "Parallel join returned null"));
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Class<I> inputType() {
        return left.inputType();
    }

    @Override
    public Class<O> outputType() {
        return outputType;
    }

    @Override
    public List<WorkflowNode> children() {
        return List.of(left.node(), right.node());
    }

    @Override
    public Set<String> referencedNodeIds() {
        return Set.of();
    }

    @Override
    public WorkflowNodeKind kind() {
        return WorkflowNodeKind.JOINED_PARALLEL;
    }
}
