package com.grab.app.channel.saleschannel.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.saleschannel.adapter.persistence.outbox.SalesChannelOutboxEvent;
import com.saleschannel.adapter.persistence.outbox.SalesChannelOutboxEventProcessor;
import com.saleschannel.adapter.persistence.outbox.SalesChannelOutboxEventProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class SalesChannelModuleOutboxConfig {

    @Bean("salesChannelOutboxEventSerializer")
    public OutboxEventSerializer salesChannelOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("salesChannelOutboxEventDispatcher")
    public OutboxEventDispatcher salesChannelOutboxEventDispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("salesChannelOutboxStore")
    public OutboxStore<SalesChannelOutboxEvent, Long> salesChannelOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(SalesChannelOutboxEvent.class),
                SalesChannelOutboxEvent.class
        );
    }

    @Bean("salesChannelOutboxRelay")
    public OutboxRelay<Long> salesChannelOutboxRelay(
            @Value("${saleschannel.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${saleschannel.outbox.hot-queue.workers:2}") int workers,
            @Value("${saleschannel.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${saleschannel.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("saleschannel", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("salesChannelDomainEventProducer")
    public DomainEventProducer salesChannelDomainEventProducer(
            @Qualifier("salesChannelOutboxStore") OutboxStore<SalesChannelOutboxEvent, Long> store,
            @Qualifier("salesChannelOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("salesChannelOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new SalesChannelOutboxEventProducer(store, serializer, relay);
    }

    @Bean("salesChannelOutboxEventProcessor")
    public SalesChannelOutboxEventProcessor salesChannelOutboxEventProcessor(
            @Qualifier("salesChannelOutboxStore") OutboxStore<SalesChannelOutboxEvent, Long> store,
            @Qualifier("salesChannelOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("salesChannelOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("salesChannelTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${saleschannel.outbox.batch-size:20}") int batchSize,
            @Value("${saleschannel.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${saleschannel.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${saleschannel.outbox.retention-ms:604800000}") long retention,
            @Qualifier("salesChannelOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new SalesChannelOutboxEventProcessor(
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

