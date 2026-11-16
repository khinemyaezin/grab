package com.grab.app.account.identity.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.outbox.infrastructure.core.port.in.DeliverOutboxEventsUseCase;
import com.grab.outbox.infrastructure.inbox.ObservedProcessedEventRepository;
import com.grab.outbox.infrastructure.inbox.ProcessedEventRetention;
import com.grab.outbox.infrastructure.inbox.jpa.JpaProcessedEventRepository;
import com.grab.outbox.infrastructure.module.OutboxModule;
import com.grab.scheduling.infrastructure.InboxScheduledJobs;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.grab.scheduling.infrastructure.OutboxScheduledJobs;
import com.grab.scheduling.infrastructure.ScheduledJob;
import com.grab.scheduling.infrastructure.ShedLockSingletonJobExecutor;
import com.identity.adapter.persistence.outbox.IdentityOutboxEvent;
import com.identity.adapter.persistence.outbox.IdentityProcessedEvent;
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
import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class IdentityModuleOutboxConfig {

    @Bean("identitySingletonJobExecutor")
    public SingletonJobExecutor identitySingletonJobExecutor(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${identity.flyway.default-schema:identity}") String schema,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        MeterRegistry meterRegistry = meters.getIfAvailable();
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "identity.scheduling", meterRegistry);
    }

    @Bean("identityOutboxModule")
    public OutboxModule<IdentityOutboxEvent> identityOutboxModule(
            JpaContext context,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            ApplicationEventPublisher publisher,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(IdentityOutboxEvent.class);
        return OutboxModule.builder("identity", IdentityOutboxEvent.class, IdentityOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .eventPublisher(publisher)
                .properties(environment)
                .meterRegistry(meters.getIfAvailable())
                .build();
    }

    @Bean("identityDomainEventProducer")
    public DomainEventProducer identityDomainEventProducer(
            @Qualifier("identityOutboxModule") OutboxModule<IdentityOutboxEvent> outbox
    ) {
        return outbox.producer();
    }

    @Bean("identityInProcessDomainEventPublisher")
    public DomainEventProducer identityInProcessDomainEventPublisher(
            ApplicationEventPublisher publisher
    ) {
        return (type, id, events) -> {
            if (events != null) {
                events.forEach(publisher::publishEvent);
            }
        };
    }

    @Bean("identityJobScheduler")
    public ModuleJobScheduler identityJobScheduler(
            @Qualifier("identityOutboxModule") OutboxModule<IdentityOutboxEvent> outbox,
            @Qualifier("identitySingletonJobExecutor") SingletonJobExecutor executor,
            @Qualifier("identityProcessedEventRetention") ProcessedEventRetention inbox,
            Environment environment
    ) {
        DeliverOutboxEventsUseCase<Long> delivery = outbox.delivery();
        List<ScheduledJob> outboxJobs = OutboxScheduledJobs.of("identity", delivery::pollOnce, outbox.cleanupJob(),
                delivery::cleanupPublished, executor, environment);
        List<ScheduledJob> jobs = new ArrayList<>(outboxJobs);
        jobs.add(InboxScheduledJobs.of("identity", inbox::purgeExpired, executor, environment));
        return ModuleJobScheduler.fromProperties("identity", jobs, environment);
    }

    @Bean("identityProcessedEventRepository")
    public ProcessedEventRepository identityProcessedEventRepository(JpaContext context, ObjectProvider<MeterRegistry> meters) {
        EntityManager entityManager = context.getEntityManagerByManagedType(IdentityProcessedEvent.class);
        ProcessedEventRepository repository =
                new JpaProcessedEventRepository<>(entityManager, IdentityProcessedEvent.class, IdentityProcessedEvent::of);
        return ObservedProcessedEventRepository.withMetrics(repository, meters);
    }

    @Bean("identityProcessedEventRetention")
    public ProcessedEventRetention identityProcessedEventRetention(
            @Qualifier("identityProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${identity.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${identity.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention("identity", processedEvents, transactionManager, retention, batchSize);
    }
}
