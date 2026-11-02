package com.grab.app.merchandising.pricing.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.pricing.adapter.persistence.outbox.PricingOutboxEvent;
import com.pricing.adapter.persistence.outbox.PricingOutboxEventProcessor;
import com.pricing.adapter.persistence.outbox.PricingOutboxEventProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class PricingModuleOutboxConfig {

    @Bean("pricingOutboxEventSerializer")
    public OutboxEventSerializer pricingOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("pricingOutboxEventDispatcher")
    public OutboxEventDispatcher pricingOutboxEventDispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("pricingOutboxStore")
    public OutboxStore<PricingOutboxEvent, Long> pricingOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(PricingOutboxEvent.class),
                PricingOutboxEvent.class
        );
    }

    @Bean("pricingOutboxRelay")
    public OutboxRelay<Long> pricingOutboxRelay(
            @Value("${pricing.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${pricing.outbox.hot-queue.workers:2}") int workers,
            @Value("${pricing.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${pricing.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("pricing", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("pricingDomainEventProducer")
    public DomainEventProducer pricingDomainEventProducer(
            @Qualifier("pricingOutboxStore") OutboxStore<PricingOutboxEvent, Long> store,
            @Qualifier("pricingOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("pricingOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new PricingOutboxEventProducer(store, serializer, relay);
    }

    @Bean("pricingOutboxEventProcessor")
    public PricingOutboxEventProcessor pricingOutboxEventProcessor(
            @Qualifier("pricingOutboxStore") OutboxStore<PricingOutboxEvent, Long> store,
            @Qualifier("pricingOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("pricingOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("pricingTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${pricing.outbox.batch-size:20}") int batchSize,
            @Value("${pricing.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${pricing.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${pricing.outbox.retention-ms:604800000}") long retention,
            @Qualifier("pricingOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new PricingOutboxEventProcessor(
                store,
                serializer,
                dispatcher,
                transactionManager,
                batchSize,
                Duration.ofMillis(retryDelay),
                Duration.ofMillis(claimTimeout),
                Duration.ofMillis(retention),
                relay
        );
    }
}

