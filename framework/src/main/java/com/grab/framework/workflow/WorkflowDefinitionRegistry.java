package com.grab.framework.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class WorkflowDefinitionRegistry {

    private final Map<String, ProcessDefinition<?>> byName;

    public static WorkflowDefinitionRegistry fromProcesses(Collection<WorkflowProcess<?>> processes) {
        List<ProcessDefinition<?>> definitions = new ArrayList<>();
        if (processes != null) {
            for (WorkflowProcess<?> process : processes) {
                definitions.add(definitionOf(process));
            }
        }
        return new WorkflowDefinitionRegistry(definitions);
    }

    public WorkflowDefinitionRegistry(Collection<ProcessDefinition<?>> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        Map<String, ProcessDefinition<?>> index = new LinkedHashMap<>();
        for (ProcessDefinition<?> definition : definitions) {
            ProcessDefinition<?> previous = index.put(definition.name(), definition);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate workflow definition: " + definition.name());
            }
        }
        this.byName = Map.copyOf(index);
    }

    private static <C> ProcessDefinition<?> definitionOf(WorkflowProcess<C> process) {
        return process.create();
    }

    public Optional<ProcessDefinition<?>> find(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    public Collection<ProcessDefinition<?>> all() {
        return byName.values();
    }
}
