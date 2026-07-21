package com.grab.framework.workflow;

import java.util.List;
import java.util.Objects;

public final class WorkflowDefinition {

    private final String name;
    private final List<WorkflowStep> steps;

    private WorkflowDefinition(String name, List<WorkflowStep> steps) {
        this.name = Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Workflow name must not be blank");
        }
        Objects.requireNonNull(steps, "steps");
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Workflow must contain at least one step");
        }
        this.steps = List.copyOf(steps);
    }

    public static WorkflowDefinition of(String name, WorkflowStep... steps) {
        return new WorkflowDefinition(name, List.of(steps));
    }

    public static WorkflowDefinition of(String name, List<WorkflowStep> steps) {
        return new WorkflowDefinition(name, steps);
    }

    public String name() {
        return name;
    }

    public List<WorkflowStep> steps() {
        return steps;
    }
}
