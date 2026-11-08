package com.grab.app.merchandising.catalog.internal.config;

import com.catalog.adapter.persistence.outbox.CatalogOutboxEvent;
import com.catalog.adapter.persistence.outbox.CatalogOutboxEventProcessor;
import com.catalog.adapter.persistence.outbox.CatalogOutboxEventProducer;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class CatalogModuleOutboxConfig {

    @Bean("catalogOutboxEventSerializer")
    public OutboxEventSerializer catalogOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("catalogOutboxEventDispatcher")
    public OutboxEventDispatcher catalogOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("catalogOutboxStore")
    public OutboxStore<CatalogOutboxEvent, Long> catalogOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(CatalogOutboxEvent.class),
                CatalogOutboxEvent.class
        );
    }

    @Bean("catalogOutboxRelay")
    public OutboxRelay<Long> catalogOutboxRelay(
            @Value("${catalog.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${catalog.outbox.hot-queue.workers:2}") int workers,
            @Value("${catalog.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${catalog.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("catalog", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("catalogDomainEventProducer")
    public DomainEventProducer catalogDomainEventProducer(
            @Qualifier("catalogOutboxStore") OutboxStore<CatalogOutboxEvent, Long> outboxStore,
            @Qualifier("catalogOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("catalogOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CatalogOutboxEventProducer(outboxStore, serializer, relay);
    }

    @Bean("catalogOutboxEventProcessor")
    public CatalogOutboxEventProcessor catalogOutboxEventProcessor(
            @Qualifier("catalogOutboxStore") OutboxStore<CatalogOutboxEvent, Long> outboxStore,
            @Qualifier("catalogOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("catalogOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("catalogTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${catalog.outbox.batch-size:20}") int batchSize,
            @Value("${catalog.outbox.retry-delay-ms:30000}") long retryDelayMs,
            @Value("${catalog.outbox.claim-timeout-ms:120000}") long claimTimeoutMs,
            @Value("${catalog.outbox.retention-ms:604800000}") long retentionMs,
            @Qualifier("catalogOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CatalogOutboxEventProcessor(
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

