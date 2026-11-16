package com.grab.outbox.infrastructure.workflow;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.OutboxDeliveryContext;
import com.grab.framework.workflow.WorkflowStepRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Runs a workflow step and emits its result events in one module transaction. When invoked from an outbox
 * delivery, the step is guarded by the module inbox: the {@code processed_event} row is written in the same
 * transaction as the step's side effects, so a redelivered request event neither re-runs the work nor re-emits
 * result signals. The failure path is guarded the same way in its own transaction.
 * <p>
 * Consumer key is {@code <module>.workflow-step}: each workflow request event is consumed by exactly one step per
 * module.
 */
public abstract class TransactionalWorkflowStepRunner extends WorkflowStepRunner {

    private static final Logger log = Loggers.getLogger(TransactionalWorkflowStepRunner.class);
    private static final String AGGREGATE_TYPE = "workflow";

    private final TransactionTemplate stepTransaction;
    private final TransactionTemplate signalTransaction;
    private final ProcessedEventRepository processedEvents;
    private final String consumer;

    protected TransactionalWorkflowStepRunner(
            String module,
            DomainEventProducer domainEventProducer,
            PlatformTransactionManager transactionManager,
            ProcessedEventRepository processedEvents
    ) {
        super(AGGREGATE_TYPE, domainEventProducer);
        this.stepTransaction = new TransactionTemplate(transactionManager);
        this.signalTransaction = new TransactionTemplate(transactionManager);
        this.signalTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.processedEvents = processedEvents;
        this.consumer = Objects.requireNonNull(module, "module") + ".workflow-step";
    }

    @Override
    public void runStep(
            String workflowId,
            Supplier<List<Event>> work,
            Function<RuntimeException, List<Event>> onFailure
    ) {
        Optional<UUID> eventId = OutboxDeliveryContext.currentEventId();
        try {
            stepTransaction.executeWithoutResult(status -> {
                if (isDuplicate(eventId, workflowId)) {
                    return;
                }
                List<Event> results = work.get();
                produce(workflowId, results);
            });
        } catch (RuntimeException exception) {
            signalTransaction.executeWithoutResult(status -> {
                if (isDuplicate(eventId, workflowId)) {
                    return;
                }
                List<Event> failureSignals = onFailure.apply(exception);
                produce(workflowId, failureSignals);
            });
        }
    }

    private boolean isDuplicate(Optional<UUID> eventId, String workflowId) {
        if (processedEvents == null || eventId.isEmpty()) {
            return false;
        }
        UUID id = eventId.get();
        boolean firstDelivery = processedEvents.markProcessed(consumer, id);
        if (!firstDelivery) {
            log.info("Skipping duplicate workflow step delivery consumer={} eventId={} workflowId={}",
                    consumer, id, workflowId);
        }
        return !firstDelivery;
    }
}
