package com.grab.framework.workflow;

public final class WorkflowCodecUnavailableException extends IllegalStateException {

    public WorkflowCodecUnavailableException(WorkflowDefinition<?, ?> definition, int payloadVersion) {
        super("No workflow payload codec for " + definition.name() + " v" + definition.version()
                + " payload version " + payloadVersion);
    }
}
