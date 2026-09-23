package com.customer.adapter.persistence.outbox;

import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

public class CustomerOutboxEventProcessor extends AbstractOutboxProcessor<CustomerOutboxEvent, Long> {
    public CustomerOutboxEventProcessor(
            OutboxStore<CustomerOutboxEvent, Long> store,
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

    public CustomerOutboxEventProcessor(
            OutboxStore<CustomerOutboxEvent, Long> store,
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

    @Scheduled(fixedDelayString = "${customer.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        processAvailableEvents();
    }

    @Scheduled(fixedDelayString = "${customer.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        cleanupPublishedEvents();
    }
}
