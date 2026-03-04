package com.inventory.infrastructure.outbox;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.inventory.infrastructure.repository.jpa.InventoryOutboxEventJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryOutboxEventProducer implements DomainEventProducer {

    private final InventoryOutboxEventJpaRepository repository;
    private final OutboxEventSerializer serializer;

    public InventoryOutboxEventProducer(
            InventoryOutboxEventJpaRepository repository,
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
        List<InventoryOutboxEvent> outboxEvents = events.stream()
                .map(event -> InventoryOutboxEvent.pending(
                        aggregateType,
                        aggregateId,
                        serializer.serialize(event),
                        now
                ))
                .toList();

        repository.saveAll(outboxEvents);
    }
}
