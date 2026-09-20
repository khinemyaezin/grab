package com.saleschannel.adapter.persistence.outbox;

import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

public class SalesChannelOutboxEventProcessor extends AbstractOutboxProcessor<SalesChannelOutboxEvent, Long> {
    public SalesChannelOutboxEventProcessor(
            OutboxStore<SalesChannelOutboxEvent, Long> store,
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

    public SalesChannelOutboxEventProcessor(
            OutboxStore<SalesChannelOutboxEvent, Long> store,
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

    @Scheduled(fixedDelayString = "${saleschannel.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        processAvailableEvents();
    }

    @Scheduled(fixedDelayString = "${saleschannel.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        cleanupPublishedEvents();
    }
}
