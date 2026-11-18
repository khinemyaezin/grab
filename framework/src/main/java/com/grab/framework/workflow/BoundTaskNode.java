package com.grab.framework.workflow;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public record BoundTaskNode<S, Q, R, T>(String id, Class<S> inputType, Class<T> outputType, TaskNode<Q, R> task,
                                       Function<? super S, ? extends Q> requestMapper,
                                       BiFunction<? super S, ? super R, ? extends T> resultCombiner) implements WorkflowNode {

    public BoundTaskNode {
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(requestMapper, "requestMapper");
        Objects.requireNonNull(resultCombiner, "resultCombiner");
    }

    public Object request(Object input) {
        S typed = inputType.cast(input);
        Q result = requestMapper.apply(typed);
        Objects.requireNonNull(result, "Task request mapper returned null");
        return task.inputType().cast(result);
    }

    public Object combine(Object input, Object result) {
        S typedInput = inputType.cast(input);
        R typedResult = task.outputType().cast(result);
        T combined = resultCombiner.apply(typedInput, typedResult);
        return outputType.cast(Objects.requireNonNull(combined, "Task result combiner returned null"));
    }

    @Override
    public WorkflowNodeKind kind() {
        return WorkflowNodeKind.BOUND_TASK;
    }

    @Override
    public List<WorkflowNode> children() {
        return List.of(task);
    }

    @Override
    public Set<String> referencedNodeIds() {
        return Set.of();
    }
}
