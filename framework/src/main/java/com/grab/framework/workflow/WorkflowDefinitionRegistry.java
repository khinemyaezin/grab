package com.grab.framework.workflow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class WorkflowDefinitionRegistry {

    private final Map<WorkflowDefinitionKey, WorkflowDefinition<?, ?>> definitions = new LinkedHashMap<>();

    public void register(WorkflowDefinition<?, ?> definition) {
        Objects.requireNonNull(definition, "definition");
        WorkflowDefinitionKey key = new WorkflowDefinitionKey(definition.name(), definition.version());
        if (definitions.putIfAbsent(key, definition) != null) {
            throw new IllegalArgumentException("Workflow definition is already registered: " + key);
        }
    }

    public Optional<WorkflowDefinition<?, ?>> find(String name, int version) {
        return Optional.ofNullable(definitions.get(new WorkflowDefinitionKey(name, version)));
    }

    public WorkflowDefinition<?, ?> require(String name, int version) {
        return find(name, version).orElseThrow(() ->
                new WorkflowDefinitionUnavailableException("Workflow definition is not registered: " + name + " v" + version));
    }

    public Map<WorkflowDefinitionKey, WorkflowDefinition<?, ?>> definitions() {
        return Map.copyOf(definitions);
    }
}
