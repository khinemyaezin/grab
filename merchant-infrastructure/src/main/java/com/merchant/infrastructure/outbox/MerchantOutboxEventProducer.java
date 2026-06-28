package com.merchant.infrastructure.outbox;

import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

public class MerchantOutboxEventProducer extends JpaOutboxDomainEventProducer<MerchantOutboxEvent> {
    public MerchantOutboxEventProducer(OutboxStore<MerchantOutboxEvent, Long> store,
                                       OutboxEventSerializer serializer) {
        super(store, serializer, MerchantOutboxEvent::pending);
    }
}
