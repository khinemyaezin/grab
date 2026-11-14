package com.grab.app.checkout.cart.internal.config;

import javax.sql.DataSource;
import org.springframework.core.env.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import com.grab.outbox.infrastructure.OutboxProcessorSettings;
import com.grab.outbox.infrastructure.OutboxObserver;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.app.shared.scheduling.ShedLockSingletonJobExecutor;
import com.cart.adapter.persistence.outbox.CartOutboxEvent;
import com.cart.adapter.persistence.outbox.CartOutboxEventProcessor;
import com.cart.adapter.persistence.outbox.CartOutboxEventProducer;
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


@Configuration(proxyBeanMethods = false)
public class CartModuleOutboxConfig {

    @Bean("cartOutboxEventSerializer")
    public OutboxEventSerializer cartOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("cartOutboxEventDispatcher")
    public OutboxEventDispatcher cartOutboxEventDispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("cartOutboxStore")
    public OutboxStore<CartOutboxEvent, Long> cartOutboxStore(JpaContext context, Environment environment) {
        OutboxClaimStrategy claimStrategy = OutboxClaimStrategies.fromProperties(environment, "cart.outbox");
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(CartOutboxEvent.class),
                CartOutboxEvent.class,
                claimStrategy
        );
    }

    @Bean("cartOutboxRelay")
    public OutboxRelay<Long> cartOutboxRelay(
            @Value("${cart.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${cart.outbox.hot-queue.workers:2}") int workers,
            @Value("${cart.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${cart.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("cart", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("cartDomainEventProducer")
    public DomainEventProducer cartDomainEventProducer(
            @Qualifier("cartOutboxStore") OutboxStore<CartOutboxEvent, Long> store,
            @Qualifier("cartOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("cartOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CartOutboxEventProducer(store, serializer, relay);
    }

    @Bean("cartSingletonJobExecutor")
    public SingletonJobExecutor cartSingletonJobExecutor(
            @Qualifier("orderDataSource") DataSource dataSource,
            @Value("${cart.flyway.default-schema:cart}") String schema,
            MeterRegistry meterRegistry,
            Environment environment
    ) {
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "cart.scheduling", meterRegistry);
    }

    @Bean("cartOutboxEventProcessor")
    public CartOutboxEventProcessor cartOutboxEventProcessor(
            @Qualifier("cartOutboxStore") OutboxStore<CartOutboxEvent, Long> outboxStore,
            @Qualifier("cartOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("cartOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("cartTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("cartOutboxRelay") OutboxRelay<Long> relay,
            @Qualifier("cartSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            OutboxObserver outboxObserver,
            Environment environment
    ) {
        return new CartOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                OutboxProcessorSettings.fromProperties(environment, "cart.outbox"),
                relay,
                outboxObserver,
                singletonJobExecutor
        );
    }
}

