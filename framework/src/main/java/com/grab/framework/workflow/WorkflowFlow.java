package com.grab.framework.workflow;

import java.util.ArrayList;
import java.util.List;

public final class WorkflowFlow<I, O> {

    private final Class<I> inputType;
    private final Class<O> outputType;
    private final List<WorkflowNode> steps;

    WorkflowFlow(Class<I> inputType, Class<O> outputType, List<WorkflowNode> steps) {
        this.inputType = inputType;
        this.outputType = outputType;
        this.steps = List.copyOf(steps);
    }

    public <N> WorkflowFlow<I, N> then(WorkflowStep<O, N> step) {
        List<WorkflowNode> next = new ArrayList<>(steps);
        next.add(step.node());
        return new WorkflowFlow<>(inputType, step.outputType(), next);
    }

    public WorkflowStep<I, O> asStep(String id) {
        SequenceNode sequence = new SequenceNode(id, steps);
        return new WorkflowStep<>(sequence, inputType, outputType);
    }

    public WorkflowDefinition<I, O> build(WorkflowIdentity identity, String rootId) {
        WorkflowStep<I, O> root = asStep(rootId);
        return WorkflowDefinition.of(identity, inputType, outputType,
                root.node(), rootId);
    }
}
