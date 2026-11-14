package com.grab.app.account.customer.internal.config;

import java.time.Duration;
import com.grab.outbox.infrastructure.inbox.ProcessedEventRetention;
import com.grab.framework.inbox.ProcessedEventRepository;
import javax.sql.DataSource;
import org.springframework.core.env.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import com.grab.outbox.infrastructure.OutboxProcessorSettings;
import com.grab.outbox.infrastructure.OutboxObserver;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.app.shared.scheduling.ShedLockSingletonJobExecutor;
import com.customer.adapter.persistence.outbox.CustomerOutboxEvent;
import com.customer.adapter.persistence.outbox.CustomerOutboxEventProcessor;
import com.customer.adapter.persistence.outbox.CustomerOutboxEventProducer;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.claim.OutboxClaimStrategies;
import com.grab.outbox.infrastructure.claim.OutboxClaimStrategy;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;


public class CustomerModuleOutboxConfig {

    @Bean("customerOutboxEventSerializer")
    public OutboxEventSerializer customerOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("customerOutboxEventDispatcher")
    public OutboxEventDispatcher customerOutboxEventDispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("customerOutboxStore")
    public OutboxStore<CustomerOutboxEvent, Long> customerOutboxStore(JpaContext context, Environment environment) {
        OutboxClaimStrategy claimStrategy = OutboxClaimStrategies.fromProperties(environment, "customer.outbox");
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(CustomerOutboxEvent.class),
                CustomerOutboxEvent.class,
                claimStrategy
        );
    }

    @Bean("customerOutboxRelay")
    public OutboxRelay<Long> customerOutboxRelay(
            @Value("${customer.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${customer.outbox.hot-queue.workers:2}") int workers,
            @Value("${customer.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${customer.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("customer", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("customerDomainEventProducer")
    public DomainEventProducer customerDomainEventProducer(
            @Qualifier("customerOutboxStore") OutboxStore<CustomerOutboxEvent, Long> store,
            @Qualifier("customerOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("customerOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CustomerOutboxEventProducer(store, serializer, relay);
    }

    @Bean("customerSingletonJobExecutor")
    public SingletonJobExecutor customerSingletonJobExecutor(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${customer.flyway.default-schema:customer}") String schema,
            MeterRegistry meterRegistry,
            Environment environment
    ) {
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "customer.scheduling", meterRegistry);
    }

    @Bean("customerOutboxEventProcessor")
    public CustomerOutboxEventProcessor customerOutboxEventProcessor(
            @Qualifier("customerOutboxStore") OutboxStore<CustomerOutboxEvent, Long> outboxStore,
            @Qualifier("customerOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("customerOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("customerTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("customerOutboxRelay") OutboxRelay<Long> relay,
            @Qualifier("customerSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            OutboxObserver outboxObserver,
            Environment environment
    ) {
        return new CustomerOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                OutboxProcessorSettings.fromProperties(environment, "customer.outbox"),
                relay,
                outboxObserver,
                singletonJobExecutor
        );
    }

    @Bean("customerProcessedEventRetention")
    public ProcessedEventRetention customerProcessedEventRetention(
            @Qualifier("customerProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("customerTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("customerSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            @Value("${customer.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${customer.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "customer", processedEvents, transactionManager, singletonJobExecutor, retention, batchSize);
    }
}
