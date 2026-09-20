package com.merchant.adapter.persistence.outbox;

import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

public class MerchantOutboxEventProducer extends JpaOutboxDomainEventProducer<MerchantOutboxEvent, Long> {
    public MerchantOutboxEventProducer(OutboxStore<MerchantOutboxEvent, Long> store,
                                       OutboxEventSerializer serializer) {
        this(store, serializer, null);
    }

    public MerchantOutboxEventProducer(OutboxStore<MerchantOutboxEvent, Long> store,
                                       OutboxEventSerializer serializer,
                                       OutboxRelay<Long> relay) {
        super(store, serializer, MerchantOutboxEvent::pending, relay);
    }
}
