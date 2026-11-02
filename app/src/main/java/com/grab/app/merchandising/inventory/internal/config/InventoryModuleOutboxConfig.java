package com.grab.app.merchandising.inventory.internal.config;

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

import java.time.Duration;

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

    @Bean("inventoryOutboxEventProcessor")
    public InventoryOutboxEventProcessor inventoryOutboxEventProcessor(
            @Qualifier("inventoryOutboxStore") OutboxStore<InventoryOutboxEvent, Long> outboxStore,
            @Qualifier("inventoryOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("inventoryOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("inventoryTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${inventory.outbox.batch-size:20}") int batchSize,
            @Value("${inventory.outbox.retry-delay-ms:30000}") long retryDelayMs,
            @Value("${inventory.outbox.claim-timeout-ms:120000}") long claimTimeoutMs,
            @Value("${inventory.outbox.retention-ms:604800000}") long retentionMs,
            @Qualifier("inventoryOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new InventoryOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                batchSize,
                Duration.ofMillis(retryDelayMs),
                Duration.ofMillis(claimTimeoutMs),
                Duration.ofMillis(retentionMs),
                relay
        );
    }
}

