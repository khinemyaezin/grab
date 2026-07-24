package com.pricing.infrastructure.outbox;

import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

public class PricingOutboxEventProcessor extends AbstractOutboxProcessor<PricingOutboxEvent, Long> {
    public PricingOutboxEventProcessor(
            OutboxStore<PricingOutboxEvent, Long> store,
            OutboxEventSerializer serializer,
            OutboxEventDispatcher dispatcher,
            PlatformTransactionManager transactionManager,
            int batchSize,
            Duration retryDelay,
            Duration claimTimeout,
            Duration retention
    ) {
        super(store, serializer, dispatcher, transactionManager, batchSize, retryDelay, claimTimeout, retention);
    }

    @Scheduled(fixedDelayString = "${pricing.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        processAvailableEvents();
    }

    @Scheduled(fixedDelayString = "${pricing.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        cleanupPublishedEvents();
    }
}
