package com.grab.app.account.identity.internal.config;

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

    @Bean("identitySingletonJobExecutor")
    public SingletonJobExecutor identitySingletonJobExecutor(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${identity.flyway.default-schema:identity}") String schema,
            MeterRegistry meterRegistry
    ) {
        return ShedLockSingletonJobExecutor.forSchema(dataSource, schema, meterRegistry);
    }

    @Bean("identityOutboxEventProcessor")
    public IdentityOutboxEventProcessor identityOutboxEventProcessor(
            @Qualifier("identityOutboxStore") OutboxStore<IdentityOutboxEvent, Long> outboxStore,
            @Qualifier("identityOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("identityOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("identityOutboxRelay") OutboxRelay<Long> relay,
            @Qualifier("identitySingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            OutboxObserver outboxObserver,
            Environment environment
    ) {
        return new IdentityOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                OutboxProcessorSettings.fromProperties(environment, "identity.outbox"),
                relay,
                outboxObserver,
                singletonJobExecutor
        );
    }
}

