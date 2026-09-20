package com.cart.infrastructure.outbox;

import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

public class CartOutboxEventProcessor extends AbstractOutboxProcessor<CartOutboxEvent, Long> {
    public CartOutboxEventProcessor(
            OutboxStore<CartOutboxEvent, Long> store,
            OutboxEventSerializer serializer,
            OutboxEventDispatcher dispatcher,
            PlatformTransactionManager transactionManager,
            int batchSize,
            Duration retryDelay,
            Duration claimTimeout,
            Duration retention
    ) {
        this(store, serializer, dispatcher, transactionManager, batchSize, retryDelay, claimTimeout, retention, OutboxRelay.noop());
    }

    public CartOutboxEventProcessor(
            OutboxStore<CartOutboxEvent, Long> store,
            OutboxEventSerializer serializer,
            OutboxEventDispatcher dispatcher,
            PlatformTransactionManager transactionManager,
            int batchSize,
            Duration retryDelay,
            Duration claimTimeout,
            Duration retention,
            OutboxRelay<Long> relay
    ) {
        super(store, serializer, dispatcher, transactionManager, batchSize, retryDelay, claimTimeout, retention, relay);
    }

    @Scheduled(fixedDelayString = "${cart.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        processAvailableEvents();
    }

    @Scheduled(fixedDelayString = "${cart.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        cleanupPublishedEvents();
    }
}
