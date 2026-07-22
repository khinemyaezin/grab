package com.grab.framework.workflow;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class WorkflowContext {

    private final String correlationId;
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    public WorkflowContext(String correlationId) {
        this.correlationId = Objects.requireNonNull(correlationId, "correlationId");
    }

    public String correlationId() {
        return correlationId;
    }

    public void put(String key, Object value) {
        attributes.put(Objects.requireNonNull(key, "key"), value);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> find(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException(
                    "Workflow attribute '" + key + "' is " + value.getClass().getName()
                            + ", expected " + type.getName());
        }
        return Optional.of((T) value);
    }

    public <T> T getRequired(String key, Class<T> type) {
        return find(key, type).orElseThrow(() ->
                new IllegalStateException("Missing workflow attribute: " + key));
    }

    public Map<String, Object> attributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public void replaceAttributes(Map<String, Object> values) {
        Objects.requireNonNull(values, "values");
        attributes.clear();
        attributes.putAll(values);
    }
}
