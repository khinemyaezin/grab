package com.inventory.infrastructure.outbox;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

public class InventoryOutboxEventProcessor extends AbstractOutboxProcessor<InventoryOutboxEvent, Long> {

    private static final Logger log = Loggers.getLogger(InventoryOutboxEventProcessor.class);

    public InventoryOutboxEventProcessor(
            OutboxStore<InventoryOutboxEvent, Long> outboxStore,
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

    @Scheduled(fixedDelayString = "${inventory.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        log.debug("Processing available inventory outbox events");
        processAvailableEvents();
        log.debug("Finished processing available inventory outbox events");
    }

    @Scheduled(fixedDelayString = "${inventory.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        log.debug("Cleaning up published inventory outbox events");
        cleanupPublishedEvents();
        log.debug("Finished cleaning up published inventory outbox events");
    }
}
