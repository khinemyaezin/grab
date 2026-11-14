package com.grab.app.merchandising.inventory.internal.config;

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
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.inventory.adapter.persistence.outbox.InventoryOutboxEvent;
import com.inventory.adapter.persistence.outbox.InventoryOutboxEventProcessor;
import com.inventory.adapter.persistence.outbox.InventoryOutboxEventProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration(proxyBeanMethods = false)
public class InventoryModuleOutboxConfig {

    @Bean("inventoryOutboxEventSerializer")
    public OutboxEventSerializer inventoryOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("inventoryOutboxEventDispatcher")
    public OutboxEventDispatcher inventoryOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("inventoryOutboxStore")
    public OutboxStore<InventoryOutboxEvent, Long> inventoryOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(InventoryOutboxEvent.class),
                InventoryOutboxEvent.class
        );
    }

    @Bean("inventoryOutboxRelay")
    public OutboxRelay<Long> inventoryOutboxRelay(
            @Value("${inventory.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${inventory.outbox.hot-queue.workers:2}") int workers,
            @Value("${inventory.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${inventory.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("inventory", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("inventoryDomainEventProducer")
    public DomainEventProducer inventoryDomainEventProducer(
            @Qualifier("inventoryOutboxStore") OutboxStore<InventoryOutboxEvent, Long> outboxStore,
            @Qualifier("inventoryOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("inventoryOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new InventoryOutboxEventProducer(outboxStore, serializer, relay);
    }

    @Bean("inventorySingletonJobExecutor")
    public SingletonJobExecutor inventorySingletonJobExecutor(
            @Qualifier("catalogDataSource") DataSource dataSource,
            @Value("${inventory.flyway.default-schema:inventory}") String schema,
            MeterRegistry meterRegistry
    ) {
        return ShedLockSingletonJobExecutor.forSchema(dataSource, schema, meterRegistry);
    }

    @Bean("inventoryOutboxEventProcessor")
    public InventoryOutboxEventProcessor inventoryOutboxEventProcessor(
            @Qualifier("inventoryOutboxStore") OutboxStore<InventoryOutboxEvent, Long> outboxStore,
            @Qualifier("inventoryOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("inventoryOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("inventoryTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("inventoryOutboxRelay") OutboxRelay<Long> relay,
            @Qualifier("inventorySingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            OutboxObserver outboxObserver,
            Environment environment
    ) {
        return new InventoryOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                OutboxProcessorSettings.fromProperties(environment, "inventory.outbox"),
                relay,
                outboxObserver,
                singletonJobExecutor
        );
    }

    @Bean("inventoryProcessedEventRetention")
    public ProcessedEventRetention inventoryProcessedEventRetention(
            @Qualifier("inventoryProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("inventoryTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("inventorySingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            @Value("${inventory.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${inventory.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "inventory", processedEvents, transactionManager, singletonJobExecutor, retention, batchSize);
    }
}
