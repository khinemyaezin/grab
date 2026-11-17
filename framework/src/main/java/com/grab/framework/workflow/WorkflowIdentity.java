package com.grab.framework.workflow;

public record WorkflowIdentity(String name, int definitionVersion, int payloadVersion) {

    public WorkflowIdentity {
        if (name == null || name.isBlank() || definitionVersion < 1 || payloadVersion < 1) {
            throw new IllegalArgumentException("Workflow identity requires a name and positive versions");
        }
    }
}
