package com.grab.workflow.infrastructure.outbox;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

public class WorkflowOutboxEventProcessor extends AbstractOutboxProcessor<WorkflowOutboxEvent, Long> {

    private static final Logger log = Loggers.getLogger(WorkflowOutboxEventProcessor.class);

    public WorkflowOutboxEventProcessor(
            OutboxStore<WorkflowOutboxEvent, Long> outboxStore,
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

    @Scheduled(fixedDelayString = "${workflows.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        log.debug("Processing available workflow outbox events");
        processAvailableEvents();
    }

    @Scheduled(fixedDelayString = "${workflows.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        log.debug("Cleaning up published workflow outbox events");
        cleanupPublishedEvents();
    }
}
