package com.identity.infrastructure.outbox;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

public class IdentityOutboxEventProcessor extends AbstractOutboxProcessor<IdentityOutboxEvent, Long> {

    private static final Logger log = Loggers.getLogger(IdentityOutboxEventProcessor.class);

    public IdentityOutboxEventProcessor(
            OutboxStore<IdentityOutboxEvent, Long> outboxStore,
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

    @Scheduled(fixedDelayString = "${identity.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        log.debug("Processing available identity outbox events");
        processAvailableEvents();
        log.debug("Finished processing available identity outbox events");
    }

    @Scheduled(fixedDelayString = "${identity.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        log.debug("Cleaning up published identity outbox events");
        cleanupPublishedEvents();
        log.debug("Finished cleaning up published identity outbox events");
    }
}
