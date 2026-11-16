package com.grab.workflow.infrastructure.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.framework.workflow.WorkflowDispatchPublisher;
import com.grab.framework.workflow.WorkflowExecutionStore;
import com.grab.framework.workflow.WorkflowHistoryStore;
import com.grab.framework.workflow.WorkflowQueryStore;
import com.grab.framework.workflow.WorkflowSignalReceiptStore;
import com.grab.framework.workflow.WorkflowStartReceiptStore;
import com.grab.framework.workflow.WorkflowFingerprint;
import com.grab.framework.workflow.WorkflowPayloadCodec;
import com.grab.outbox.infrastructure.core.port.in.DeliverOutboxEventsUseCase;
import com.grab.outbox.infrastructure.module.OutboxModule;
import com.grab.scheduling.infrastructure.LocalSingletonJobExecutor;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.grab.scheduling.infrastructure.OutboxScheduledJobs;
import com.grab.scheduling.infrastructure.ScheduledJob;
import com.grab.workflow.infrastructure.outbox.WorkflowOutboxEvent;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowExecutionStore;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowHistoryStore;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowQueryStore;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowSignalReceiptStore;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowStartReceiptStore;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowExecutionJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowHistoryJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowNodeExecutionJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowSignalReceiptJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowStartReceiptJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowTimerJpaRepository;
import com.grab.workflow.infrastructure.service.JpaWorkflowTimerScheduler;
import com.grab.workflow.infrastructure.service.OutboxWorkflowDispatchPublisher;
import com.grab.workflow.infrastructure.serialization.JacksonWorkflowFingerprint;
import com.grab.workflow.infrastructure.serialization.JacksonWorkflowPayloadCodec;
import io.micrometer.core.instrument.MeterRegistry;
import com.grab.workflow.infrastructure.entity.WorkflowStartReceiptEntity;
import com.grab.workflow.infrastructure.entity.WorkflowSignalReceiptEntity;
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
        return new JacksonWorkflowPayloadCodec();
    }

    @Bean
    public WorkflowFingerprint workflowFingerprint() {
        return new JacksonWorkflowFingerprint();
    }

    @Bean
    public WorkflowDispatchPublisher workflowDispatchPublisher(
            @Qualifier("workflowDomainEventProducer") DomainEventProducer domainEventProducer
    ) {
        return new OutboxWorkflowDispatchPublisher(domainEventProducer);
    }

    @Bean
    public WorkflowExecutionStore workflowExecutionStore(
            WorkflowExecutionJpaRepository executionRepository,
            WorkflowNodeExecutionJpaRepository nodeExecutionRepository,
            WorkflowStartReceiptJpaRepository startReceiptRepository,
            WorkflowPayloadCodec payloadCodec
    ) {
        return new JpaWorkflowExecutionStore(executionRepository, nodeExecutionRepository, startReceiptRepository, payloadCodec);
    }

    @Bean
    public WorkflowStartReceiptStore workflowStartReceiptStore(
            WorkflowStartReceiptJpaRepository startReceiptRepository, JpaContext context
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(WorkflowStartReceiptEntity.class);
        return new JpaWorkflowStartReceiptStore(startReceiptRepository, entityManager);
    }

    @Bean
    public WorkflowSignalReceiptStore workflowSignalReceiptStore(
            WorkflowSignalReceiptJpaRepository signalReceiptRepository, JpaContext context
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(WorkflowSignalReceiptEntity.class);
        return new JpaWorkflowSignalReceiptStore(signalReceiptRepository, entityManager);
    }

    @Bean
    public WorkflowHistoryStore workflowHistoryStore(WorkflowHistoryJpaRepository historyRepository) {
        return new JpaWorkflowHistoryStore(historyRepository);
    }

    @Bean
    public WorkflowQueryStore workflowQueryStore(
            WorkflowExecutionJpaRepository executionRepository,
            WorkflowNodeExecutionJpaRepository nodeExecutionRepository,
            WorkflowHistoryJpaRepository historyRepository
    ) {
        return new JpaWorkflowQueryStore(executionRepository, nodeExecutionRepository, historyRepository);
    }

    @Bean
    public JpaWorkflowTimerScheduler workflowTimerScheduler(WorkflowTimerJpaRepository timerRepository) {
        return new JpaWorkflowTimerScheduler(timerRepository);
    }

}
