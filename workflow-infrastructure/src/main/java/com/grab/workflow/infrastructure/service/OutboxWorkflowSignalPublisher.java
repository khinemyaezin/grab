package com.grab.workflow.infrastructure.service;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.workflow.WorkflowSignalPublisher;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class OutboxWorkflowSignalPublisher implements WorkflowSignalPublisher {

    private final DomainEventProducer domainEventProducer;

    @Override
    public void publish(String workflowId, List<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        domainEventProducer.produce("workflow", workflowId, events);
    }
}
