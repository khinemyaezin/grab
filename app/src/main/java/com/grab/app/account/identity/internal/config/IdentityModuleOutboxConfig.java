package com.grab.app.account.identity.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.identity.adapter.persistence.outbox.IdentityOutboxEvent;
import com.identity.adapter.persistence.outbox.IdentityOutboxEventProcessor;
import com.identity.adapter.persistence.outbox.IdentityOutboxEventProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class IdentityModuleOutboxConfig {

    @Bean("identityOutboxEventSerializer")
    public OutboxEventSerializer identityOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("identityOutboxEventDispatcher")
    public OutboxEventDispatcher identityOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("identityOutboxStore")
    public OutboxStore<IdentityOutboxEvent, Long> identityOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(IdentityOutboxEvent.class),
                IdentityOutboxEvent.class
        );
    }

    @Bean("identityOutboxRelay")
    public OutboxRelay<Long> identityOutboxRelay(
            @Value("${identity.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${identity.outbox.hot-queue.workers:2}") int workers,
            @Value("${identity.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${identity.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("identity", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("identityDomainEventProducer")
    public DomainEventProducer identityDomainEventProducer(
            @Qualifier("identityOutboxStore") OutboxStore<IdentityOutboxEvent, Long> outboxStore,
            @Qualifier("identityOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("identityOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new IdentityOutboxEventProducer(outboxStore, serializer, relay);
    }

    @Bean("identityOutboxEventProcessor")
    public IdentityOutboxEventProcessor identityOutboxEventProcessor(
            @Qualifier("identityOutboxStore") OutboxStore<IdentityOutboxEvent, Long> outboxStore,
            @Qualifier("identityOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("identityOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${identity.outbox.batch-size:20}") int batchSize,
            @Value("${identity.outbox.retry-delay-ms:30000}") long retryDelayMs,
            @Value("${identity.outbox.claim-timeout-ms:120000}") long claimTimeoutMs,
            @Value("${identity.outbox.retention-ms:604800000}") long retentionMs,
            @Qualifier("identityOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new IdentityOutboxEventProcessor(
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

