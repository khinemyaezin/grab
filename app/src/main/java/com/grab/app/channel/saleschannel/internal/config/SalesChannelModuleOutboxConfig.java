package com.grab.app.channel.saleschannel.internal.config;

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
import com.saleschannel.adapter.persistence.outbox.SalesChannelOutboxEvent;
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
public class SalesChannelModuleOutboxConfig {

    @Bean("salesChannelSingletonJobExecutor")
    public SingletonJobExecutor salesChannelSingletonJobExecutor(
            @Qualifier("channelDataSource") DataSource dataSource,
            @Value("${saleschannel.flyway.default-schema:saleschannel}") String schema,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        MeterRegistry meterRegistry = meters.getIfAvailable();
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "saleschannel.scheduling", meterRegistry);
    }

    @Bean("salesChannelOutboxModule")
    public OutboxModule<SalesChannelOutboxEvent> salesChannelOutboxModule(
            JpaContext context,
            @Qualifier("salesChannelTransactionManager") PlatformTransactionManager transactionManager,
            ApplicationEventPublisher publisher,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(SalesChannelOutboxEvent.class);
        return OutboxModule.builder("saleschannel", SalesChannelOutboxEvent.class, SalesChannelOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .eventPublisher(publisher)
                .properties(environment)
                .meterRegistry(meters.getIfAvailable())
                .build();
    }

    @Bean("salesChannelDomainEventProducer")
    public DomainEventProducer salesChannelDomainEventProducer(
            @Qualifier("salesChannelOutboxModule") OutboxModule<SalesChannelOutboxEvent> outbox
    ) {
        return outbox.producer();
    }

    @Bean("salesChannelInProcessDomainEventPublisher")
    public DomainEventProducer salesChannelInProcessDomainEventPublisher(
            ApplicationEventPublisher publisher
    ) {
        return (type, id, events) -> {
            if (events != null) {
                events.forEach(publisher::publishEvent);
            }
        };
    }

    @Bean("salesChannelInboxDecorator")
    public UnaryOperator<ProcessedEventRepository> salesChannelInboxDecorator(ObjectProvider<MeterRegistry> meters) {
        return repository -> ObservedProcessedEventRepository.withMetrics(repository, meters);
    }

    @Bean("salesChannelProcessedEventRetention")
    public ProcessedEventRetention salesChannelProcessedEventRetention(
            @Qualifier("salesChannelProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("salesChannelTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${saleschannel.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${saleschannel.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "saleschannel", processedEvents, transactionManager, retention, batchSize);
    }

    @Bean("salesChannelJobScheduler")
    public ModuleJobScheduler salesChannelJobScheduler(
            @Qualifier("salesChannelOutboxModule") OutboxModule<SalesChannelOutboxEvent> outbox,
            @Qualifier("salesChannelSingletonJobExecutor") SingletonJobExecutor executor,
            @Qualifier("salesChannelProcessedEventRetention") ProcessedEventRetention inbox,
            Environment environment
    ) {
        DeliverOutboxEventsUseCase<Long> delivery = outbox.delivery();
        List<ScheduledJob> outboxJobs = OutboxScheduledJobs.of("saleschannel", delivery::pollOnce, outbox.cleanupJob(),
                delivery::cleanupPublished, executor, environment);
        List<ScheduledJob> jobs = new ArrayList<>(outboxJobs);
        jobs.add(InboxScheduledJobs.of("saleschannel", inbox::purgeExpired, executor, environment));
        return ModuleJobScheduler.fromProperties("saleschannel", jobs, environment);
    }
}
