package com.grab.workflow.infrastructure.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.framework.workflow.*;
import com.grab.framework.workflow.impl.EventDrivenWorkflowEngine;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxDomainEventProducer;
import com.grab.outbox.infrastructure.core.port.in.DeliverOutboxEventsUseCase;
import com.grab.outbox.infrastructure.module.OutboxModule;
import com.grab.scheduling.infrastructure.LocalSingletonJobExecutor;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.grab.scheduling.infrastructure.OutboxScheduledJobs;
import com.grab.scheduling.infrastructure.ScheduledJob;
import com.grab.workflow.infrastructure.mapper.WorkflowInstanceMapper;
import com.grab.workflow.infrastructure.outbox.WorkflowOutboxEvent;
import com.grab.workflow.infrastructure.repository.jpa.JpaWorkflowStore;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowCorrelationJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowInstanceJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowSignalLogJpaRepository;
import com.grab.workflow.infrastructure.service.OutboxWorkflowSignalPublisher;
import com.grab.workflow.infrastructure.service.TransactionalWorkflowEngine;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
public class WorkflowInfraConfig {

    @Bean
    public JpaWorkflowStore jpaWorkflowStore(
            WorkflowInstanceJpaRepository workflowInstanceJpaRepository,
            WorkflowInstanceMapper mapper,
            WorkflowCorrelationJpaRepository correlationJpaRepository,
            WorkflowSignalLogJpaRepository signalLogJpaRepository
    ) {
        return new JpaWorkflowStore(
                workflowInstanceJpaRepository,
                mapper,
                correlationJpaRepository,
                signalLogJpaRepository
        );
    }

    @Bean("workflowsOutboxModule")
    public OutboxModule<WorkflowOutboxEvent> workflowsOutboxModule(
            JpaContext context,
            @Qualifier("workflowsTransactionManager") PlatformTransactionManager transactionManager,
            ApplicationEventPublisher publisher,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(WorkflowOutboxEvent.class);
        return OutboxModule.builder("workflows", WorkflowOutboxEvent.class, WorkflowOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .eventPublisher(publisher)
                .properties(environment)
                .meterRegistry(meters.getIfAvailable())
                .build();
    }

    @Bean("workflowDomainEventProducer")
    public DomainEventProducer workflowDomainEventProducer(
            @Qualifier("workflowsOutboxModule") OutboxModule<WorkflowOutboxEvent> outbox
    ) {
        return outbox.producer();
    }

    @Bean("workflowsJobScheduler")
    public ModuleJobScheduler workflowsJobScheduler(
            @Qualifier("workflowsOutboxModule") OutboxModule<WorkflowOutboxEvent> outbox,
            @Qualifier("workflowsSingletonJobExecutor") ObjectProvider<SingletonJobExecutor> configuredExecutor,
            Environment environment
    ) {
        SingletonJobExecutor executor = configuredExecutor.getIfAvailable(LocalSingletonJobExecutor::new);
        DeliverOutboxEventsUseCase<Long> delivery = outbox.delivery();
        List<ScheduledJob> jobs = OutboxScheduledJobs.of("workflows", delivery::pollOnce, outbox.cleanupJob(),
                delivery::cleanupPublished, executor, environment);
        return ModuleJobScheduler.fromProperties("workflows", jobs, environment);
    }

    @Bean
    public WorkflowPayloadCodec workflowPayloadCodec() {
        return new WorkflowPayloadCodec();
    }

    @Bean
    public WorkflowSignalPublisher workflowSignalPublisher(
            @Qualifier("workflowDomainEventProducer") DomainEventProducer domainEventProducer
    ) {
        return new OutboxWorkflowSignalPublisher(domainEventProducer);
    }

    @Bean
    public WorkflowEngine workflowEngine(
            WorkflowStore workflowStore,
            WorkflowDefinitionRegistry workflowDefinitionRegistry,
            WorkflowPayloadCodec workflowPayloadCodec,
            WorkflowSignalPublisher workflowSignalPublisher,
            WorkflowLifecycleListener workflowLifecycleListener,
            IdGenerator idGenerator,
            @Qualifier("workflowsTransactionManager") PlatformTransactionManager workflowsTransactionManager
    ) {
        EventDrivenWorkflowEngine engine = new EventDrivenWorkflowEngine(
                workflowStore,
                workflowDefinitionRegistry,
                workflowPayloadCodec,
                workflowSignalPublisher,
                idGenerator,
                workflowLifecycleListener
        );
        return new TransactionalWorkflowEngine(engine, workflowsTransactionManager);
    }
}
