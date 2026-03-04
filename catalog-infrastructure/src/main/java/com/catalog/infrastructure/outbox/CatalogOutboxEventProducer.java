package com.catalog.infrastructure.outbox;

import com.catalog.infrastructure.repository.jpa.CatalogOutboxEventJpaRepository;
import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.OutboxEventSerializer;

import java.time.LocalDateTime;
import java.util.List;

public class CatalogOutboxEventProducer implements DomainEventProducer {

    private final CatalogOutboxEventJpaRepository repository;
    private final OutboxEventSerializer serializer;

    public CatalogOutboxEventProducer(
            CatalogOutboxEventJpaRepository repository,
            OutboxEventSerializer serializer
    ) {
        this.repository = repository;
        this.serializer = serializer;
    }

    @Override
    public void produce(String aggregateType, String aggregateId, List<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<CatalogOutboxEvent> outboxEvents = events.stream()
                .map(event -> CatalogOutboxEvent.pending(
                        aggregateType,
                        aggregateId,
                        serializer.serialize(event),
                        now
                ))
                .toList();

        repository.saveAll(outboxEvents);
    }
}
