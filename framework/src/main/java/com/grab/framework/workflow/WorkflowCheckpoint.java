package com.grab.framework.workflow;

import java.util.Objects;

public record WorkflowCheckpoint(String stepName, Object output) {

    public WorkflowCheckpoint {
        Objects.requireNonNull(stepName, "stepName");
        if (stepName.isBlank()) {
            throw new IllegalArgumentException("stepName must not be blank");
        }
    }
}
