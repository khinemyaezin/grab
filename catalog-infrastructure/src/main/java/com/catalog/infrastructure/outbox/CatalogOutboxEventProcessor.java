package com.catalog.infrastructure.outbox;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

public class CatalogOutboxEventProcessor extends AbstractOutboxProcessor<CatalogOutboxEvent, Long> {

    private static final Logger log = Loggers.getLogger(CatalogOutboxEventProcessor.class);

    public CatalogOutboxEventProcessor(
            OutboxStore<CatalogOutboxEvent, Long> outboxStore,
            OutboxEventSerializer serializer,
            OutboxEventDispatcher dispatcher,
            PlatformTransactionManager transactionManager,
            int batchSize,
            Duration retryDelay,
            Duration claimTimeout,
            Duration retention
    ) {
        super(outboxStore, serializer, dispatcher, transactionManager, batchSize, retryDelay, claimTimeout, retention);
    }

    @Scheduled(fixedDelayString = "${catalog.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        log.debug("Processing available catalog outbox events");
        processAvailableEvents();
        log.debug("Finished processing available catalog outbox events");
    }

    @Scheduled(fixedDelayString = "${catalog.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        log.debug("Cleaning up published catalog outbox events");
        cleanupPublishedEvents();
        log.debug("Finished cleaning up published catalog outbox events");
    }
}
