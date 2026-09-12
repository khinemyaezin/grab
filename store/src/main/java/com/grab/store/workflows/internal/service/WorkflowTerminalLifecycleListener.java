package com.grab.store.workflows.internal.service;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowLifecycleListener;
import com.grab.store.shared.sse.WorkflowTerminalUiEvent;
import com.grab.store.workflows.internal.workflows.createsellableproduct.CreateSellableProductContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public final class WorkflowTerminalLifecycleListener implements WorkflowLifecycleListener {

    private final ApplicationEventPublisher events;

    public WorkflowTerminalLifecycleListener(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Override
    public void onTerminal(WorkflowInstance instance, Object context) {
        if (!(context instanceof CreateSellableProductContext createContext)) {
            return;
        }
        if (createContext.createdBy() == null || createContext.createdBy().isBlank()) {
            return;
        }
        events.publishEvent(new WorkflowTerminalUiEvent(
                createContext.createdBy(),
                createContext.scopeId(),
                instance.id(),
                instance.workflowName(),
                instance.status().name(),
                instance.idempotencyKey().orElse(null),
                instance.errorMessage().orElse(null)
        ));
    }
}
