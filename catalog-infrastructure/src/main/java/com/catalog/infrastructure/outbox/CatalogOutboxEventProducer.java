package com.catalog.infrastructure.outbox;

import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

public class CatalogOutboxEventProducer extends JpaOutboxDomainEventProducer<CatalogOutboxEvent> {

    public CatalogOutboxEventProducer(
            OutboxStore<CatalogOutboxEvent, Long> outboxStore,
            OutboxEventSerializer serializer
    ) {
        super(
                outboxStore,
                serializer,
                CatalogOutboxEvent::pending
        );
    }
}
