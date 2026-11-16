package com.grab.framework.workflow;

import java.util.Objects;

public record WorkflowDefinitionKey(String name, int version) {

    public WorkflowDefinitionKey {
        name = Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (version < 1) {
            throw new IllegalArgumentException("version must be positive");
        }
    }
}
