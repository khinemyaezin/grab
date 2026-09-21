package com.cart.adapter.persistence.outbox;

import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxDomainEventProducer;

public class CartOutboxEventProducer extends JpaOutboxDomainEventProducer<CartOutboxEvent, Long> {
    public CartOutboxEventProducer(
            OutboxStore<CartOutboxEvent, Long> store,
            OutboxEventSerializer serializer
    ) {
        this(store, serializer, null);
    }

    public CartOutboxEventProducer(
            OutboxStore<CartOutboxEvent, Long> store,
            OutboxEventSerializer serializer,
            OutboxRelay<Long> relay
    ) {
        super(store, serializer, CartOutboxEvent::pending, relay);
    }
}
