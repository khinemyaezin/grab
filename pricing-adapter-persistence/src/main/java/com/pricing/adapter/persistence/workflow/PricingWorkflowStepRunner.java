package com.pricing.adapter.persistence.workflow;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.outbox.infrastructure.workflow.TransactionalWorkflowStepRunner;
import org.springframework.transaction.PlatformTransactionManager;

public class PricingWorkflowStepRunner extends TransactionalWorkflowStepRunner {

    public static final String MODULE = "pricing";

    public PricingWorkflowStepRunner(DomainEventProducer domainEventProducer,
                              PlatformTransactionManager transactionManager) {
        this(domainEventProducer, transactionManager, null);
    }

    public PricingWorkflowStepRunner(DomainEventProducer domainEventProducer,
                              PlatformTransactionManager transactionManager,
                              ProcessedEventRepository processedEvents) {
        super(MODULE, domainEventProducer, transactionManager, processedEvents);
    }
}
