package com.region.adapter.persistence.outbox;

import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

public class RegionOutboxEventProcessor extends AbstractOutboxProcessor<RegionOutboxEvent, Long> {
    public RegionOutboxEventProcessor(
            OutboxStore<RegionOutboxEvent, Long> store,
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

    @Scheduled(fixedDelayString = "${region.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        processAvailableEvents();
    }

    @Scheduled(fixedDelayString = "${region.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        cleanupPublishedEvents();
    }
}
