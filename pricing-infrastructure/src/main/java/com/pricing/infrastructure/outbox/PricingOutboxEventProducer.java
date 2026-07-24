package com.pricing.infrastructure.outbox;

import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

public class PricingOutboxEventProducer extends JpaOutboxDomainEventProducer<PricingOutboxEvent> {
    public PricingOutboxEventProducer(
            OutboxStore<PricingOutboxEvent, Long> store,
            OutboxEventSerializer serializer
    ) {
        super(store, serializer, PricingOutboxEvent::pending);
    }
}
