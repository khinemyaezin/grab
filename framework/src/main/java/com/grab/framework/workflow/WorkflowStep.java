package com.grab.framework.workflow;

public final class WorkflowStep<I, O> {

    private final WorkflowNode node;
    private final Class<I> inputType;
    private final Class<O> outputType;

    WorkflowStep(WorkflowNode node, Class<I> inputType, Class<O> outputType) {
        this.node = node;
        this.inputType = inputType;
        this.outputType = outputType;
    }

    WorkflowNode node() {
        return node;
    }

    public Class<I> inputType() {
        return inputType;
    }

    public Class<O> outputType() {
        return outputType;
    }
}
