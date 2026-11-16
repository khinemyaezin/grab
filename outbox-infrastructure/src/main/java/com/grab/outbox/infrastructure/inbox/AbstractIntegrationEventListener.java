package com.grab.outbox.infrastructure.inbox;

import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.outbox.OutboxDeliveryContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

public abstract class AbstractIntegrationEventListener {

    private final TransactionTemplate transaction;
    private final ProcessedEventRepository processedEvents;

    protected AbstractIntegrationEventListener(
            PlatformTransactionManager transactionManager,
            ProcessedEventRepository processedEvents
    ) {
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.processedEvents = processedEvents;
    }

    protected final void processDelivery(String consumer, Runnable action) {
        UUID eventId = OutboxDeliveryContext.currentEventId().orElseThrow(() ->
                new IllegalStateException("Missing outbox delivery context for " + consumer));
        transaction.executeWithoutResult(status -> {
            if (processedEvents.markProcessed(consumer, eventId)) {
                action.run();
            }
        });
    }
}
