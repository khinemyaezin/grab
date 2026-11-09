package com.grab.app.account.customer.internal.config;

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
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

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
    public OutboxStore<CustomerOutboxEvent, Long> customerOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(CustomerOutboxEvent.class),
                CustomerOutboxEvent.class
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

    @Bean("customerOutboxEventProcessor")
    public CustomerOutboxEventProcessor customerOutboxEventProcessor(
            @Qualifier("customerOutboxStore") OutboxStore<CustomerOutboxEvent, Long> store,
            @Qualifier("customerOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("customerOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("customerTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${customer.outbox.batch-size:20}") int batchSize,
            @Value("${customer.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${customer.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${customer.outbox.retention-ms:604800000}") long retention,
            @Qualifier("customerOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CustomerOutboxEventProcessor(
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

