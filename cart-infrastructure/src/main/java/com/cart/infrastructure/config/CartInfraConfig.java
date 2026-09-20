package com.cart.infrastructure.config;

import com.cart.domain.repository.CartRepository;
import com.cart.infrastructure.mapper.jpa.CartJpaAssembler;
import com.cart.infrastructure.outbox.CartOutboxEvent;
import com.cart.infrastructure.outbox.CartOutboxEventProcessor;
import com.cart.infrastructure.outbox.CartOutboxEventProducer;
import com.cart.infrastructure.repository.jpa.CartJpaRepository;
import com.cart.infrastructure.repository.jpa.impl.CartPersistenceExecutor;
import com.cart.infrastructure.repository.jpa.impl.DefaultCartRepository;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration
@Import(CartDomainConfig.class)
public class CartInfraConfig {
    @Bean("cartOutboxEventSerializer")
    OutboxEventSerializer serializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("cartOutboxEventDispatcher")
    OutboxEventDispatcher dispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("cartOutboxStore")
    OutboxStore<CartOutboxEvent, Long> outboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(CartOutboxEvent.class),
                CartOutboxEvent.class
        );
    }

    @Bean("cartOutboxRelay")
    OutboxRelay<Long> cartOutboxRelay(
            @Value("${cart.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${cart.outbox.hot-queue.workers:2}") int workers,
            @Value("${cart.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${cart.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("cart", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("cartDomainEventProducer")
    DomainEventProducer domainEventProducer(
            @Qualifier("cartOutboxStore") OutboxStore<CartOutboxEvent, Long> store,
            @Qualifier("cartOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("cartOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CartOutboxEventProducer(store, serializer, relay);
    }

    @Bean
    CartOutboxEventProcessor processor(
            @Qualifier("cartOutboxStore") OutboxStore<CartOutboxEvent, Long> store,
            @Qualifier("cartOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("cartOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("cartTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${cart.outbox.batch-size:20}") int batchSize,
            @Value("${cart.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${cart.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${cart.outbox.retention-ms:604800000}") long retention,
            @Qualifier("cartOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CartOutboxEventProcessor(
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

    @Bean("cartPersistenceExecutor")
    PersistenceExecutor persistenceExecutor() {
        return new CartPersistenceExecutor();
    }

    @Bean
    CartJpaAssembler cartAssembler(IdMapper ids) {
        return new CartJpaAssembler(ids);
    }

    @Bean
    CartRepository cartRepository(
            CartJpaRepository carts,
            CartJpaAssembler assembler,
            @Qualifier("cartDomainEventProducer") DomainEventProducer events,
            @Qualifier("cartPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultCartRepository(carts, assembler, events, executor);
    }
}
