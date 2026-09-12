package com.grab.framework.workflow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ProcessDefinition<C> {

    private final String name;
    private final Class<C> contextType;
    private final List<StepDefinition<C>> steps;
    private final Map<String, StepDefinition<C>> byName;

    private ProcessDefinition(String name, Class<C> contextType, List<StepDefinition<C>> steps) {
        this.name = Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Workflow name must not be blank");
        }
        this.contextType = Objects.requireNonNull(contextType, "contextType");
        Objects.requireNonNull(steps, "steps");
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Workflow must contain at least one step");
        }
        this.steps = List.copyOf(steps);
        Map<String, StepDefinition<C>> index = new LinkedHashMap<>();
        for (StepDefinition<C> step : this.steps) {
            if (index.put(step.name(), step) != null) {
                throw new IllegalArgumentException("Duplicate step name: " + step.name());
            }
        }
        this.byName = Map.copyOf(index);
    }

    @SafeVarargs
    public static <C> ProcessDefinition<C> of(String name, Class<C> contextType, StepDefinition<C>... steps) {
        return new ProcessDefinition<>(name, contextType, List.of(steps));
    }

    public static <C> ProcessDefinition<C> of(String name, Class<C> contextType, List<StepDefinition<C>> steps) {
        return new ProcessDefinition<>(name, contextType, steps);
    }

    public String name() {
        return name;
    }

    public Class<C> contextType() {
        return contextType;
    }

    public List<StepDefinition<C>> steps() {
        return steps;
    }

    public Optional<StepDefinition<C>> step(String stepName) {
        return Optional.ofNullable(byName.get(stepName));
    }

    public int indexOf(String stepName) {
        for (int index = 0; index < steps.size(); index++) {
            if (steps.get(index).name().equals(stepName)) {
                return index;
            }
        }
        return -1;
    }
}
