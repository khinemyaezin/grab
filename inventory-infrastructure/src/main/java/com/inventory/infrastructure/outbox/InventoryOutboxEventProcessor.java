package com.inventory.infrastructure.outbox;

import com.grab.framework.domain.Event;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxStatus;
import com.inventory.infrastructure.repository.jpa.InventoryOutboxEventJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class InventoryOutboxEventProcessor {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final InventoryOutboxEventJpaRepository repository;
    private final OutboxEventSerializer serializer;
    private final OutboxEventDispatcher dispatcher;
    private final TransactionTemplate claimTransactionTemplate;
    private final TransactionTemplate publishTransactionTemplate;
    private final int batchSize;
    private final Duration retryDelay;
    private final Duration claimTimeout;

    public InventoryOutboxEventProcessor(
            InventoryOutboxEventJpaRepository repository,
            OutboxEventSerializer serializer,
            OutboxEventDispatcher dispatcher,
            PlatformTransactionManager transactionManager,
            int batchSize,
            Duration retryDelay,
            Duration claimTimeout
    ) {
        this.repository = repository;
        this.serializer = serializer;
        this.dispatcher = dispatcher;
        this.batchSize = batchSize;
        this.retryDelay = retryDelay;
        this.claimTimeout = claimTimeout;
        this.claimTransactionTemplate = new TransactionTemplate(transactionManager);
        this.publishTransactionTemplate = new TransactionTemplate(transactionManager);
        this.publishTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Scheduled(fixedDelayString = "${inventory.outbox.fixed-delay-ms:5000}")
    public void processAvailableEvents() {
        List<Long> eventIds = claimBatch();
        for (Long eventId : eventIds) {
            publishEvent(eventId);
        }
    }

    private List<Long> claimBatch() {
        List<Long> eventIds = claimTransactionTemplate.execute(status -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime staleBefore = now.minus(claimTimeout);

            List<InventoryOutboxEvent> events = repository.findBatchForProcessing(
                    List.of(OutboxStatus.NEW, OutboxStatus.FAILED),
                    OutboxStatus.PROCESSING,
                    now,
                    staleBefore,
                    PageRequest.of(0, batchSize)
            );

            events.forEach(event -> event.markProcessing(now));

            return events.stream()
                    .map(InventoryOutboxEvent::getId)
                    .toList();
        });

        return eventIds == null ? List.of() : eventIds;
    }

    private void publishEvent(Long eventId) {
        publishTransactionTemplate.executeWithoutResult(status -> repository.findById(eventId)
                .filter(event -> event.getStatus() == OutboxStatus.PROCESSING)
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
