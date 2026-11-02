package com.grab.app.account.merchant.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.merchant.adapter.persistence.outbox.MerchantOutboxEvent;
import com.merchant.adapter.persistence.outbox.MerchantOutboxEventProcessor;
import com.merchant.adapter.persistence.outbox.MerchantOutboxEventProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class MerchantModuleOutboxConfig {

    @Bean("merchantOutboxEventSerializer")
    public OutboxEventSerializer merchantOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("merchantOutboxEventDispatcher")
    public OutboxEventDispatcher merchantOutboxEventDispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("merchantOutboxStore")
    public OutboxStore<MerchantOutboxEvent, Long> merchantOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(MerchantOutboxEvent.class),
                MerchantOutboxEvent.class
        );
    }

    @Bean("merchantOutboxRelay")
    public OutboxRelay<Long> merchantOutboxRelay(
            @Value("${merchant.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${merchant.outbox.hot-queue.workers:2}") int workers,
            @Value("${merchant.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${merchant.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("merchant", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("merchantDomainEventProducer")
    public DomainEventProducer merchantDomainEventProducer(
            @Qualifier("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> store,
            @Qualifier("merchantOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("merchantOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new MerchantOutboxEventProducer(store, serializer, relay);
    }

    @Bean("merchantOutboxEventProcessor")
    public MerchantOutboxEventProcessor merchantOutboxEventProcessor(
            @Qualifier("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> store,
            @Qualifier("merchantOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("merchantOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("merchantTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${merchant.outbox.batch-size:20}") int batchSize,
            @Value("${merchant.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${merchant.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${merchant.outbox.retention-ms:604800000}") long retention,
            @Qualifier("merchantOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new MerchantOutboxEventProcessor(
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

