package com.saleschannel.infrastructure.outbox;

import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

public class SalesChannelOutboxEventProducer extends JpaOutboxDomainEventProducer<SalesChannelOutboxEvent, Long> {
    public SalesChannelOutboxEventProducer(
            OutboxStore<SalesChannelOutboxEvent, Long> store,
            OutboxEventSerializer serializer
    ) {
        this(store, serializer, null);
    }

    public SalesChannelOutboxEventProducer(
            OutboxStore<SalesChannelOutboxEvent, Long> store,
            OutboxEventSerializer serializer,
            OutboxRelay<Long> relay
    ) {
        super(store, serializer, SalesChannelOutboxEvent::pending, relay);
    }
}
