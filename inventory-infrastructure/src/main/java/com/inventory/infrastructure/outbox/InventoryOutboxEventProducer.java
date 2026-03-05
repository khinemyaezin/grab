package com.inventory.infrastructure.outbox;

import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

public class InventoryOutboxEventProducer extends JpaOutboxDomainEventProducer<InventoryOutboxEvent> {

    public InventoryOutboxEventProducer(
            OutboxStore<InventoryOutboxEvent, Long> outboxStore,
            OutboxEventSerializer serializer
    ) {
        super(
                outboxStore,
                serializer,
                InventoryOutboxEvent::pending
        );
    }
}
