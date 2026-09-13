package com.grab.store.workflows.internal.service;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowLifecycleListener;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.store.shared.sse.WorkflowTerminalUiEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public final class WorkflowTerminalLifecycleListener implements WorkflowLifecycleListener {

    private final ApplicationEventPublisher events;
    private final Map<String, WorkflowTerminalContextAdapter<?>> adapters;

    public WorkflowTerminalLifecycleListener(
            ApplicationEventPublisher events,
            List<WorkflowTerminalContextAdapter<?>> adapters
    ) {
        this.events = events;
        this.adapters = adapters.stream().collect(Collectors.toUnmodifiableMap(
                WorkflowTerminalContextAdapter::workflowName,
                Function.identity()
        ));
    }

    @Override
    public void onTerminal(WorkflowInstance instance, Object context) {
        WorkflowTerminalContextAdapter<?> adapter = adapters.get(instance.workflowName());
        if (adapter == null) {
            return;
        }
        adapter.fromContext(context).ifPresent(details -> publish(instance, details));
    }

    private void publish(WorkflowInstance instance, WorkflowTerminalDetails details) {
        String createdBy = details.createdBy();
        if (createdBy == null || createdBy.isBlank()) {
            return;
        }
        events.publishEvent(new WorkflowTerminalUiEvent(
                createdBy,
                details.scopeId(),
                instance.id(),
                instance.workflowName(),
                instance.status().name(),
                instance.idempotencyKey().orElse(null),
                instance.errorMessage().orElse(null),
                isFailedOrCompensated(instance) && details.partiallyApplied()
        ));
    }

    private static boolean isFailedOrCompensated(WorkflowInstance instance) {
        return instance.status() == WorkflowStatus.FAILED || instance.status() == WorkflowStatus.COMPENSATED;
    }
}
