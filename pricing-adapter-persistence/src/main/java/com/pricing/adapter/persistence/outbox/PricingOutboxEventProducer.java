package com.pricing.adapter.persistence.outbox;

import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

public class PricingOutboxEventProducer extends JpaOutboxDomainEventProducer<PricingOutboxEvent, Long> {
    public PricingOutboxEventProducer(
            OutboxStore<PricingOutboxEvent, Long> store,
            OutboxEventSerializer serializer
    ) {
        this(store, serializer, null);
    }

    public PricingOutboxEventProducer(
            OutboxStore<PricingOutboxEvent, Long> store,
            OutboxEventSerializer serializer,
            OutboxRelay<Long> relay
    ) {
        super(store, serializer, PricingOutboxEvent::pending, relay);
    }
}
