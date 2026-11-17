package com.grab.framework.workflow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class WorkflowDefinitionRegistry {

    private final Map<WorkflowDefinitionKey, WorkflowDefinition<?, ?>> definitions = new LinkedHashMap<>();

    private final Map<WorkflowIdentity, WorkflowDefinitionProvider<?, ?>> providers = new LinkedHashMap<>();

    public void register(WorkflowDefinitionProvider<?, ?> provider) {
        WorkflowDefinition<?, ?> definition = provider.definition();
        if (!definition.identity().equals(provider.identity())) {
            throw new IllegalArgumentException("Workflow provider identity differs from its definition");
        }
        register(definition);
        providers.put(definition.identity(), provider);
    }

    public void validateStart(WorkflowDefinition<?, ?> definition, String callerScope, Object input) {
        WorkflowDefinitionProvider<?, ?> provider = providers.get(definition.identity());
        if (provider != null) {
            validateStart(provider, callerScope, input);
        }
    }

    private static <I> void validateStart(WorkflowDefinitionProvider<I, ?> provider, String callerScope, Object input) {
        Class<I> inputType = provider.definition().inputType();
        I typed = inputType.cast(input);
        provider.validateStart(callerScope, typed);
    }

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
