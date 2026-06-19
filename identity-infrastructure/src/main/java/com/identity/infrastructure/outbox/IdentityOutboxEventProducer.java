package com.identity.infrastructure.outbox;

import com.grab.framework.domain.Event;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

import java.util.List;

public class IdentityOutboxEventProducer extends JpaOutboxDomainEventProducer<IdentityOutboxEvent> {

    private static final Logger log = Loggers.getLogger(IdentityOutboxEventProducer.class);

    public IdentityOutboxEventProducer(
            OutboxStore<IdentityOutboxEvent, Long> outboxStore,
            OutboxEventSerializer serializer
    ) {
        super(
                outboxStore,
                serializer,
                IdentityOutboxEvent::pending
        );
    }

    @Override
    public void produce(String aggregateType, String aggregateId, List<Event> events) {
        int eventCount = events == null ? 0 : events.size();
        if (eventCount == 0) {
            log.debug("Skipping identity outbox production for aggregateType={}, aggregateId={} because no events were provided", aggregateType, aggregateId);
            return;
        }

        log.info(
                "Producing {} identity outbox event(s) for aggregateType={}, aggregateId={}",
                eventCount,
                aggregateType,
                aggregateId
        );
        super.produce(aggregateType, aggregateId, events);
    }
}
