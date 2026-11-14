package com.grab.app.account.merchant.internal.config;

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
import com.merchant.adapter.persistence.outbox.MerchantOutboxEvent;
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
public class MerchantModuleOutboxConfig {

    @Bean("merchantSingletonJobExecutor")
    public SingletonJobExecutor merchantSingletonJobExecutor(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${merchant.flyway.default-schema:merchant}") String schema,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        MeterRegistry meterRegistry = meters.getIfAvailable();
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "merchant.scheduling", meterRegistry);
    }

    @Bean("merchantOutboxModule")
    public OutboxModule<MerchantOutboxEvent> merchantOutboxModule(
            JpaContext context,
            @Qualifier("merchantTransactionManager") PlatformTransactionManager transactionManager,
            ApplicationEventPublisher publisher,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(MerchantOutboxEvent.class);
        return OutboxModule.builder("merchant", MerchantOutboxEvent.class, MerchantOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .eventPublisher(publisher)
                .properties(environment)
                .meterRegistry(meters.getIfAvailable())
                .build();
    }

    @Bean("merchantDomainEventProducer")
    public DomainEventProducer merchantDomainEventProducer(
            @Qualifier("merchantOutboxModule") OutboxModule<MerchantOutboxEvent> outbox
    ) {
        return outbox.producer();
    }

    @Bean("merchantInboxDecorator")
    public UnaryOperator<ProcessedEventRepository> merchantInboxDecorator(ObjectProvider<MeterRegistry> meters) {
        return repository -> ObservedProcessedEventRepository.withMetrics(repository, meters);
    }

    @Bean("merchantProcessedEventRetention")
    public ProcessedEventRetention merchantProcessedEventRetention(
            @Qualifier("merchantProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("merchantTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${merchant.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${merchant.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "merchant", processedEvents, transactionManager, retention, batchSize);
    }

    @Bean("merchantJobScheduler")
    public ModuleJobScheduler merchantJobScheduler(
            @Qualifier("merchantOutboxModule") OutboxModule<MerchantOutboxEvent> outbox,
            @Qualifier("merchantSingletonJobExecutor") SingletonJobExecutor executor,
            @Qualifier("merchantProcessedEventRetention") ProcessedEventRetention inbox,
            Environment environment
    ) {
        DeliverOutboxEventsUseCase<Long> delivery = outbox.delivery();
        List<ScheduledJob> outboxJobs = OutboxScheduledJobs.of("merchant", delivery::pollOnce, outbox.cleanupJob(),
                delivery::cleanupPublished, executor, environment);
        List<ScheduledJob> jobs = new ArrayList<>(outboxJobs);
        jobs.add(InboxScheduledJobs.of("merchant", inbox::purgeExpired, executor, environment));
        return ModuleJobScheduler.fromProperties("merchant", jobs, environment);
    }
}
