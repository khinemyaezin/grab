package com.grab.workflow.infrastructure.service;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.workflow.WorkflowDispatch;
import com.grab.framework.workflow.WorkflowTaskDispatchEvent;
import com.grab.framework.workflow.WorkflowDispatchPublisher;

import java.util.List;
import java.util.Objects;

public final class OutboxWorkflowDispatchPublisher implements WorkflowDispatchPublisher {

    private final DomainEventProducer domainEventProducer;

    public OutboxWorkflowDispatchPublisher(DomainEventProducer domainEventProducer) {
        this.domainEventProducer = Objects.requireNonNull(domainEventProducer, "domainEventProducer");
    }

    @Override
    public void publish(String executionId, List<WorkflowDispatch> dispatches) {
        Objects.requireNonNull(executionId, "executionId");
        Objects.requireNonNull(dispatches, "dispatches");
        if (dispatches.isEmpty()) {
            return;
        }
        List<Event> events = dispatches.stream()
                .map(dispatch -> toEvent(executionId, dispatch))
                .toList();
        domainEventProducer.produce("workflow", executionId, events);
    }

    private Event toEvent(String executionId, WorkflowDispatch dispatch) {
        return new WorkflowTaskDispatchEvent(
                dispatch.callerScope(),
                dispatch.definitionName(),
                dispatch.definitionVersion(),
                dispatch.payloadVersion(),
                executionId,
                dispatch.nodeId(),
                dispatch.operation(),
                dispatch.logicalOperationId(),
                dispatch.attemptId(),
                dispatch.attempt(),
                dispatch.phase(),
                dispatch.input()
        );
    }
}
