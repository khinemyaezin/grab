package com.saleschannel.infrastructure.workflow;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.workflow.WorkflowStepRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SalesChannelWorkflowStepRunner extends WorkflowStepRunner {
    private final TransactionTemplate stepTransaction;
    private final TransactionTemplate signalTransaction;

    public SalesChannelWorkflowStepRunner(DomainEventProducer domainEventProducer,
                                          PlatformTransactionManager transactionManager) {
        super("workflow", domainEventProducer);
        this.stepTransaction = new TransactionTemplate(transactionManager);
        this.signalTransaction = new TransactionTemplate(transactionManager);
        this.signalTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void runStep(
            String workflowId,
            Supplier<List<Event>> work,
            Function<RuntimeException, List<Event>> onFailure
    ) {
        try {
            stepTransaction.executeWithoutResult(status -> produce(workflowId, work.get()));
        } catch (RuntimeException exception) {
            signalTransaction.executeWithoutResult(status -> produce(workflowId, onFailure.apply(exception)));
        }
    }
}
