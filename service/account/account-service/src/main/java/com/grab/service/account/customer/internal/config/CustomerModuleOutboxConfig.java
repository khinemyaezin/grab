package com.grab.service.account.customer.internal.config;

import com.customer.adapter.persistence.outbox.CustomerOutboxEvent;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.outbox.OutboxEventDispatcher;
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
public class CustomerModuleOutboxConfig {

    @Bean("customerSingletonJobExecutor")
    public SingletonJobExecutor customerSingletonJobExecutor(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${customer.flyway.default-schema:customer}") String schema,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        MeterRegistry meterRegistry = meters.getIfAvailable();
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "customer.scheduling", meterRegistry);
    }

    @Bean("customerOutboxModule")
    public OutboxModule<CustomerOutboxEvent> customerOutboxModule(
            JpaContext context,
            @Qualifier("customerTransactionManager") PlatformTransactionManager transactionManager,
            OutboxEventDispatcher dispatcher,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(CustomerOutboxEvent.class);
        return OutboxModule.builder("customer", CustomerOutboxEvent.class, CustomerOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .dispatcher(dispatcher)
                .properties(environment)
                .meterRegistry(meters.getIfAvailable())
                .build();
    }

    @Bean("customerDomainEventProducer")
    public DomainEventProducer customerDomainEventProducer(
            @Qualifier("customerOutboxModule") OutboxModule<CustomerOutboxEvent> outbox
    ) {
        return outbox.producer();
    }

    @Bean("customerInProcessDomainEventPublisher")
    public DomainEventProducer customerInProcessDomainEventPublisher(
            ApplicationEventPublisher publisher
    ) {
        return (type, id, events) -> {
            if (events != null) {
                events.forEach(publisher::publishEvent);
            }
        };
    }

    @Bean("customerInboxDecorator")
    public UnaryOperator<ProcessedEventRepository> customerInboxDecorator(ObjectProvider<MeterRegistry> meters) {
        return repository -> ObservedProcessedEventRepository.withMetrics(repository, meters);
    }

    @Bean("customerProcessedEventRetention")
    public ProcessedEventRetention customerProcessedEventRetention(
            @Qualifier("customerProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("customerTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${customer.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${customer.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "customer", processedEvents, transactionManager, retention, batchSize);
    }

    @Bean("customerJobScheduler")
    public ModuleJobScheduler customerJobScheduler(
            @Qualifier("customerOutboxModule") OutboxModule<CustomerOutboxEvent> outbox,
            @Qualifier("customerSingletonJobExecutor") SingletonJobExecutor executor,
            @Qualifier("customerProcessedEventRetention") ProcessedEventRetention inbox,
            Environment environment
    ) {
        DeliverOutboxEventsUseCase<Long> delivery = outbox.delivery();
        List<ScheduledJob> outboxJobs = OutboxScheduledJobs.of("customer", delivery::pollOnce, outbox.cleanupJob(),
                delivery::cleanupPublished, executor, environment);
        List<ScheduledJob> jobs = new ArrayList<>(outboxJobs);
        jobs.add(InboxScheduledJobs.of("customer", inbox::purgeExpired, executor, environment));
        return ModuleJobScheduler.fromProperties("customer", jobs, environment);
    }
}
