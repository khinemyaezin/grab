package com.region.adapter.persistence.outbox;

import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

public class RegionOutboxEventProducer extends JpaOutboxDomainEventProducer<RegionOutboxEvent, Long> {
    public RegionOutboxEventProducer(
            OutboxStore<RegionOutboxEvent, Long> store,
            OutboxEventSerializer serializer,
            OutboxRelay<Long> relay
    ) {
        super(store, serializer, RegionOutboxEvent::pending, relay);
    }
}
