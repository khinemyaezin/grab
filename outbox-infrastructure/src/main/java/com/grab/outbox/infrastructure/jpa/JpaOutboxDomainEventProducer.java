package com.grab.outbox.infrastructure.jpa;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.OutboxRowFactory;
import com.grab.outbox.infrastructure.OutboxStore;

import java.time.LocalDateTime;
import java.util.List;

public class JpaOutboxDomainEventProducer<T extends OutboxEntry<?>> implements DomainEventProducer {

    private final OutboxStore<T, ?> outboxStore;
    private final OutboxEventSerializer serializer;
    private final OutboxRowFactory<T> rowFactory;

    public JpaOutboxDomainEventProducer(
            OutboxStore<T, ?> outboxStore,
            OutboxEventSerializer serializer,
            OutboxRowFactory<T> rowFactory
    ) {
        this.outboxStore = outboxStore;
        this.serializer = serializer;
        this.rowFactory = rowFactory;
    }

    @Override
    public void produce(String aggregateType, String aggregateId, List<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<T> outboxEvents = events.stream()
                .map(event -> rowFactory.create(aggregateType, aggregateId, serializer.serialize(event), now))
                .toList();
        outboxStore.saveAll(outboxEvents);
    }
}
