package com.grab.store.shared.workflow;

import com.grab.framework.domain.Event;
import com.grab.framework.workflow.WorkflowStepRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class FakeWorkflowStepRunner extends WorkflowStepRunner {

    private final List<Event> emittedEvents = new ArrayList<>();
    private final List<String> workflowIds = new ArrayList<>();
    private RuntimeException lastException;

    public FakeWorkflowStepRunner() {
        super("workflow", null);
    }

    @Override
    public void runStep(
            String workflowId,
            Supplier<List<Event>> work,
            Function<RuntimeException, List<Event>> onFailure
    ) {
        workflowIds.add(workflowId);
        try {
            List<Event> events = work.get();
            if (events != null && !events.isEmpty()) {
                emittedEvents.addAll(events);
            }
        } catch (RuntimeException exception) {
            this.lastException = exception;
            List<Event> failureEvents = onFailure.apply(exception);
            if (failureEvents != null && !failureEvents.isEmpty()) {
                emittedEvents.addAll(failureEvents);
            }
        }
    }

    public List<Event> emittedEvents() {
        return List.copyOf(emittedEvents);
    }

    public <T extends Event> List<T> eventsOfType(Class<T> eventType) {
        return emittedEvents.stream()
                .filter(eventType::isInstance)
                .map(eventType::cast)
                .toList();
    }

    public <T extends Event> T firstEventOfType(Class<T> eventType) {
        return eventsOfType(eventType).stream().findFirst().orElse(null);
    }

    public String lastWorkflowId() {
        return workflowIds.isEmpty() ? null : workflowIds.getLast();
    }

    public RuntimeException lastException() {
        return lastException;
    }

    public void clear() {
        emittedEvents.clear();
        workflowIds.clear();
        lastException = null;
    }
}
