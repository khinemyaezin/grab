package com.grab.workflow.infrastructure.outbox;

import com.grab.framework.domain.Event;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

import java.util.List;

public class WorkflowOutboxEventProducer extends JpaOutboxDomainEventProducer<WorkflowOutboxEvent, Long> {

    private static final Logger log = Loggers.getLogger(WorkflowOutboxEventProducer.class);

    public WorkflowOutboxEventProducer(
            OutboxStore<WorkflowOutboxEvent, Long> outboxStore,
            OutboxEventSerializer serializer
    ) {
        this(outboxStore, serializer, null);
    }

    public WorkflowOutboxEventProducer(
            OutboxStore<WorkflowOutboxEvent, Long> outboxStore,
            OutboxEventSerializer serializer,
            OutboxRelay<Long> relay
    ) {
        super(outboxStore, serializer, WorkflowOutboxEvent::pending, relay);
    }

    @Override
    public void produce(String aggregateType, String aggregateId, List<Event> events) {
        int eventCount = events == null ? 0 : events.size();
        if (eventCount == 0) {
            return;
        }
        log.info(
                "Producing {} workflow outbox event(s) for aggregateType={}, aggregateId={}",
                eventCount,
                aggregateType,
                aggregateId
        );
        super.produce(aggregateType, aggregateId, events);
    }
}
