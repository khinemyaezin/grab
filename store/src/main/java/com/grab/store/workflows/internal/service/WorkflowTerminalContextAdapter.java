package com.grab.store.workflows.internal.service;

import java.util.Optional;

public interface WorkflowTerminalContextAdapter<C> {

    String workflowName();

    Class<C> contextType();

    WorkflowTerminalDetails from(C context);

    default Optional<WorkflowTerminalDetails> fromContext(Object context) {
        if (!contextType().isInstance(context)) {
            return Optional.empty();
        }
        return Optional.of(from(contextType().cast(context)));
    }
}
