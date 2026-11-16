package com.grab.app.merchandising.pricing.internal.config;

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
import com.pricing.adapter.persistence.outbox.PricingOutboxEvent;
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
public class PricingModuleOutboxConfig {

    @Bean("pricingSingletonJobExecutor")
    public SingletonJobExecutor pricingSingletonJobExecutor(
            @Qualifier("catalogDataSource") DataSource dataSource,
            @Value("${pricing.flyway.default-schema:pricing}") String schema,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        MeterRegistry meterRegistry = meters.getIfAvailable();
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "pricing.scheduling", meterRegistry);
    }

    @Bean("pricingOutboxModule")
    public OutboxModule<PricingOutboxEvent> pricingOutboxModule(
            JpaContext context,
            @Qualifier("pricingTransactionManager") PlatformTransactionManager transactionManager,
            ApplicationEventPublisher publisher,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(PricingOutboxEvent.class);
        return OutboxModule.builder("pricing", PricingOutboxEvent.class, PricingOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .eventPublisher(publisher)
                .properties(environment)
                .meterRegistry(meters.getIfAvailable())
                .build();
    }

    @Bean("pricingDomainEventProducer")
    public DomainEventProducer pricingDomainEventProducer(
            @Qualifier("pricingOutboxModule") OutboxModule<PricingOutboxEvent> outbox
    ) {
        return outbox.producer();
    }

    @Bean("pricingInboxDecorator")
    public UnaryOperator<ProcessedEventRepository> pricingInboxDecorator(ObjectProvider<MeterRegistry> meters) {
        return repository -> ObservedProcessedEventRepository.withMetrics(repository, meters);
    }

    @Bean("pricingProcessedEventRetention")
    public ProcessedEventRetention pricingProcessedEventRetention(
            @Qualifier("pricingProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("pricingTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${pricing.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${pricing.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "pricing", processedEvents, transactionManager, retention, batchSize);
    }

    @Bean("pricingJobScheduler")
    public ModuleJobScheduler pricingJobScheduler(
            @Qualifier("pricingOutboxModule") OutboxModule<PricingOutboxEvent> outbox,
            @Qualifier("pricingSingletonJobExecutor") SingletonJobExecutor executor,
            @Qualifier("pricingProcessedEventRetention") ProcessedEventRetention inbox,
            Environment environment
    ) {
        DeliverOutboxEventsUseCase<Long> delivery = outbox.delivery();
        List<ScheduledJob> outboxJobs = OutboxScheduledJobs.of("pricing", delivery::pollOnce, outbox.cleanupJob(),
                delivery::cleanupPublished, executor, environment);
        List<ScheduledJob> jobs = new ArrayList<>(outboxJobs);
        jobs.add(InboxScheduledJobs.of("pricing", inbox::purgeExpired, executor, environment));
        return ModuleJobScheduler.fromProperties("pricing", jobs, environment);
    }
}
