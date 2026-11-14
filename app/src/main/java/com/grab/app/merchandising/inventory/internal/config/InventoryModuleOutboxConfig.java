package com.grab.app.merchandising.inventory.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.outbox.infrastructure.core.port.in.DeliverOutboxEventsUseCase;
import com.grab.outbox.infrastructure.inbox.ObservedProcessedEventRepository;
import com.grab.outbox.infrastructure.inbox.ProcessedEventRetention;
import com.grab.outbox.infrastructure.module.OutboxModule;
import com.grab.scheduling.infrastructure.InboxScheduledJobs;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.grab.scheduling.infrastructure.OutboxScheduledJobs;
import com.grab.scheduling.infrastructure.ScheduledJob;
import com.grab.scheduling.infrastructure.ShedLockSingletonJobExecutor;
import com.inventory.adapter.persistence.outbox.InventoryOutboxEvent;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class InventoryModuleOutboxConfig {

    @Bean("inventorySingletonJobExecutor")
    public SingletonJobExecutor inventorySingletonJobExecutor(
            @Qualifier("catalogDataSource") DataSource dataSource,
            @Value("${inventory.flyway.default-schema:inventory}") String schema,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        MeterRegistry meterRegistry = meters.getIfAvailable();
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "inventory.scheduling", meterRegistry);
    }

    @Bean("inventoryOutboxModule")
    public OutboxModule<InventoryOutboxEvent> inventoryOutboxModule(
            JpaContext context,
            @Qualifier("inventoryTransactionManager") PlatformTransactionManager transactionManager,
            ApplicationEventPublisher publisher,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(InventoryOutboxEvent.class);
        return OutboxModule.builder("inventory", InventoryOutboxEvent.class, InventoryOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .eventPublisher(publisher)
                .properties(environment)
                .meterRegistry(meters.getIfAvailable())
                .build();
    }

    @Bean("inventoryDomainEventProducer")
    public DomainEventProducer inventoryDomainEventProducer(
            @Qualifier("inventoryOutboxModule") OutboxModule<InventoryOutboxEvent> outbox
    ) {
        return outbox.producer();
    }

    @Bean("inventoryInboxDecorator")
    public UnaryOperator<ProcessedEventRepository> inventoryInboxDecorator(ObjectProvider<MeterRegistry> meters) {
        return repository -> ObservedProcessedEventRepository.withMetrics(repository, meters);
    }

    @Bean("inventoryProcessedEventRetention")
    public ProcessedEventRetention inventoryProcessedEventRetention(
            @Qualifier("inventoryProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("inventoryTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${inventory.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${inventory.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "inventory", processedEvents, transactionManager, retention, batchSize);
    }

    @Bean("inventoryJobScheduler")
    public ModuleJobScheduler inventoryJobScheduler(
            @Qualifier("inventoryOutboxModule") OutboxModule<InventoryOutboxEvent> outbox,
            @Qualifier("inventorySingletonJobExecutor") SingletonJobExecutor executor,
            @Qualifier("inventoryProcessedEventRetention") ProcessedEventRetention inbox,
            Environment environment
    ) {
        DeliverOutboxEventsUseCase<Long> delivery = outbox.delivery();
        List<ScheduledJob> outboxJobs = OutboxScheduledJobs.of("inventory", delivery::pollOnce, outbox.cleanupJob(),
                delivery::cleanupPublished, executor, environment);
        List<ScheduledJob> jobs = new ArrayList<>(outboxJobs);
        jobs.add(InboxScheduledJobs.of("inventory", inbox::purgeExpired, executor, environment));
        return ModuleJobScheduler.fromProperties("inventory", jobs, environment);
    }
}
