package com.grab.app.merchandising.catalog.internal.config;

import com.catalog.adapter.persistence.outbox.CatalogOutboxEvent;
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
public class CatalogModuleOutboxConfig {

    @Bean("catalogSingletonJobExecutor")
    public SingletonJobExecutor catalogSingletonJobExecutor(
            @Qualifier("catalogDataSource") DataSource dataSource,
            @Value("${catalog.flyway.default-schema:catalog}") String schema,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        MeterRegistry meterRegistry = meters.getIfAvailable();
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "catalog.scheduling", meterRegistry);
    }

    @Bean("catalogOutboxModule")
    public OutboxModule<CatalogOutboxEvent> catalogOutboxModule(
            JpaContext context,
            @Qualifier("catalogTransactionManager") PlatformTransactionManager transactionManager,
            ApplicationEventPublisher publisher,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(CatalogOutboxEvent.class);
        return OutboxModule.builder("catalog", CatalogOutboxEvent.class, CatalogOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .eventPublisher(publisher)
                .properties(environment)
                .meterRegistry(meters.getIfAvailable())
                .build();
    }

    @Bean("catalogDomainEventProducer")
    public DomainEventProducer catalogDomainEventProducer(
            @Qualifier("catalogOutboxModule") OutboxModule<CatalogOutboxEvent> outbox
    ) {
        return outbox.producer();
    }

    @Bean("catalogInboxDecorator")
    public UnaryOperator<ProcessedEventRepository> catalogInboxDecorator(ObjectProvider<MeterRegistry> meters) {
        return repository -> ObservedProcessedEventRepository.withMetrics(repository, meters);
    }

    @Bean("catalogProcessedEventRetention")
    public ProcessedEventRetention catalogProcessedEventRetention(
            @Qualifier("catalogProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("catalogTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${catalog.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${catalog.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "catalog", processedEvents, transactionManager, retention, batchSize);
    }

    @Bean("catalogJobScheduler")
    public ModuleJobScheduler catalogJobScheduler(
            @Qualifier("catalogOutboxModule") OutboxModule<CatalogOutboxEvent> outbox,
            @Qualifier("catalogSingletonJobExecutor") SingletonJobExecutor executor,
            @Qualifier("catalogProcessedEventRetention") ProcessedEventRetention inbox,
            Environment environment
    ) {
        DeliverOutboxEventsUseCase<Long> delivery = outbox.delivery();
        List<ScheduledJob> outboxJobs = OutboxScheduledJobs.of("catalog", delivery::pollOnce, outbox.cleanupJob(),
                delivery::cleanupPublished, executor, environment);
        List<ScheduledJob> jobs = new ArrayList<>(outboxJobs);
        jobs.add(InboxScheduledJobs.of("catalog", inbox::purgeExpired, executor, environment));
        return ModuleJobScheduler.fromProperties("catalog", jobs, environment);
    }
}
