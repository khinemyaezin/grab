package com.customer.adapter.persistence.outbox;

import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

public class CustomerOutboxEventProducer extends JpaOutboxDomainEventProducer<CustomerOutboxEvent, Long> {
    public CustomerOutboxEventProducer(
            OutboxStore<CustomerOutboxEvent, Long> store,
            OutboxEventSerializer serializer
    ) {
        this(store, serializer, null);
    }

    public CustomerOutboxEventProducer(
            OutboxStore<CustomerOutboxEvent, Long> store,
            OutboxEventSerializer serializer,
            OutboxRelay<Long> relay
    ) {
        super(store, serializer, CustomerOutboxEvent::pending, relay);
    }
}
