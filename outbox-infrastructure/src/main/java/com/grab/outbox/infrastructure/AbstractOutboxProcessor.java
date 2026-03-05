package com.grab.outbox.infrastructure;

import com.grab.framework.domain.Event;
import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public abstract class AbstractOutboxProcessor<T extends OutboxEntry<ID>, ID> {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final OutboxStore<T, ID> outboxStore;
    private final OutboxEventSerializer serializer;
    private final OutboxEventDispatcher dispatcher;
    private final TransactionTemplate claimTransactionTemplate;
    private final TransactionTemplate publishTransactionTemplate;
    private final TransactionTemplate cleanupTransactionTemplate;
    private final int batchSize;
    private final Duration retryDelay;
    private final Duration claimTimeout;
    private final Duration retention;

    protected AbstractOutboxProcessor(
            OutboxStore<T, ID> outboxStore,
            OutboxEventSerializer serializer,
            OutboxEventDispatcher dispatcher,
            PlatformTransactionManager transactionManager,
            int batchSize,
            Duration retryDelay,
            Duration claimTimeout,
            Duration retention
    ) {
        this.outboxStore = outboxStore;
        this.serializer = serializer;
        this.dispatcher = dispatcher;
        this.batchSize = batchSize;
        this.retryDelay = retryDelay;
        this.claimTimeout = claimTimeout;
        this.retention = retention;
        this.claimTransactionTemplate = new TransactionTemplate(transactionManager);
        this.publishTransactionTemplate = new TransactionTemplate(transactionManager);
        this.publishTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.cleanupTransactionTemplate = new TransactionTemplate(transactionManager);
        this.cleanupTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    protected void processAvailableEvents() {
        List<ClaimedOutboxEvent<ID>> claimedEvents = claimBatch();
        for (ClaimedOutboxEvent<ID> claimedEvent : claimedEvents) {
            publishEvent(claimedEvent);
        }
    }

    protected void cleanupPublishedEvents() {
        cleanupTransactionTemplate.executeWithoutResult(status ->
                outboxStore.deletePublishedOlderThan(LocalDateTime.now().minus(retention)));
    }

    private List<ClaimedOutboxEvent<ID>> claimBatch() {
        List<ClaimedOutboxEvent<ID>> claimedEvents = claimTransactionTemplate.execute(status -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime staleBefore = now.minus(claimTimeout);

            List<T> events = outboxStore.findBatchForProcessing(
                    List.of(OutboxStatus.NEW, OutboxStatus.FAILED),
                    OutboxStatus.PROCESSING,
                    now,
                    staleBefore,
                    batchSize
            );

            return events.stream()
                    .map(event -> {
                        String claimToken = UUID.randomUUID().toString();
                        event.markProcessing(now, claimToken);
                        return new ClaimedOutboxEvent<>(event.getId(), claimToken);
                    })
                    .toList();
        });

        return claimedEvents == null ? List.of() : claimedEvents;
    }

    private void publishEvent(ClaimedOutboxEvent<ID> claimedEvent) {
        publishTransactionTemplate.executeWithoutResult(status -> outboxStore.findById(claimedEvent.id())
                .filter(event -> event.getStatus() == OutboxStatus.PROCESSING)
                .filter(event -> claimedEvent.claimToken().equals(event.getClaimToken()))
                .ifPresent(event -> {
                    try {
                        Event payload = serializer.deserialize(event.getEventType(), event.getPayload());
                        dispatcher.dispatch(payload);
                        event.markPublished(LocalDateTime.now());
                    } catch (RuntimeException exception) {
                        event.markFailed(LocalDateTime.now(), abbreviate(exception), retryDelay);
                    }
                }));
    }

    private String abbreviate(RuntimeException exception) {
        String message = exception.getMessage();
        String value = message == null || message.isBlank() ? exception.getClass().getName() : message;
        if (value.length() <= MAX_ERROR_LENGTH) {
            return value;
        }

        return value.substring(0, MAX_ERROR_LENGTH);
    }
}
