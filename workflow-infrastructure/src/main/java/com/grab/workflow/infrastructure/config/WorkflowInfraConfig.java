package com.grab.workflow.infrastructure.config;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.ObjectProvider;
import com.grab.outbox.infrastructure.OutboxProcessorSettings;
import com.grab.outbox.infrastructure.OutboxObserver;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.workflow.*;
import com.grab.framework.workflow.impl.EventDrivenWorkflowEngine;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.grab.workflow.infrastructure.mapper.WorkflowInstanceMapper;
import com.grab.workflow.infrastructure.outbox.WorkflowOutboxEvent;
import com.grab.workflow.infrastructure.outbox.WorkflowOutboxEventProcessor;
import com.grab.workflow.infrastructure.outbox.WorkflowOutboxEventProducer;
import com.grab.workflow.infrastructure.repository.jpa.JpaWorkflowStore;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowCorrelationJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowInstanceJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowSignalLogJpaRepository;
import com.grab.workflow.infrastructure.service.OutboxWorkflowSignalPublisher;
import com.grab.workflow.infrastructure.service.TransactionalWorkflowEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean("workflowOutboxEventSerializer")
    public OutboxEventSerializer workflowOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("workflowOutboxEventDispatcher")
    public OutboxEventDispatcher workflowOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("workflowOutboxStore")
    public OutboxStore<WorkflowOutboxEvent, Long> workflowOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(WorkflowOutboxEvent.class),
                WorkflowOutboxEvent.class
        );
    }

    @Bean("workflowOutboxRelay")
    public OutboxRelay<Long> workflowOutboxRelay(
            @Value("${workflows.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${workflows.outbox.hot-queue.workers:2}") int workers,
            @Value("${workflows.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${workflows.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("workflow", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("workflowDomainEventProducer")
    public DomainEventProducer workflowDomainEventProducer(
            @Qualifier("workflowOutboxStore") OutboxStore<WorkflowOutboxEvent, Long> outboxStore,
            @Qualifier("workflowOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("workflowOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new WorkflowOutboxEventProducer(outboxStore, serializer, relay);
    }

    @Bean
    public WorkflowOutboxEventProcessor workflowOutboxEventProcessor(
            @Qualifier("workflowOutboxStore") OutboxStore<WorkflowOutboxEvent, Long> outboxStore,
            @Qualifier("workflowOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("workflowOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("workflowsTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("workflowOutboxRelay") OutboxRelay<Long> relay,
            @Qualifier("workflowsSingletonJobExecutor") ObjectProvider<SingletonJobExecutor> singletonJobExecutor,
            ObjectProvider<OutboxObserver> outboxObserver,
            Environment environment
    ) {
        return new WorkflowOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                OutboxProcessorSettings.fromProperties(environment, "workflows.outbox"),
                relay,
                outboxObserver.getIfAvailable(OutboxObserver::noop),
                singletonJobExecutor.getIfAvailable(SingletonJobExecutor::local)
        );
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
