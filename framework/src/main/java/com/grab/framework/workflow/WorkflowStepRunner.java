package com.grab.framework.workflow;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class WorkflowStepRunner {
    private final String AGGREGATE_TYPE;
    private final DomainEventProducer domainEventProducer;

    protected WorkflowStepRunner(String aggregateType, DomainEventProducer eventProducer) {
        this.domainEventProducer = eventProducer;
        this.AGGREGATE_TYPE = aggregateType;
    }

    protected void produce(String workflowId, List<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        domainEventProducer.produce(AGGREGATE_TYPE, workflowId, events);
    }

    public abstract void runStep(
            String workflowId,
            Supplier<List<Event>> work,
            Function<RuntimeException, List<Event>> onFailure
    );
}
