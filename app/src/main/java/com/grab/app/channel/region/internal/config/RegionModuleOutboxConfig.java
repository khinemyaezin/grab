package com.grab.app.channel.region.internal.config;

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
import com.region.adapter.persistence.outbox.RegionOutboxEvent;
import com.region.adapter.persistence.outbox.RegionOutboxEventProcessor;
import com.region.adapter.persistence.outbox.RegionOutboxEventProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration(proxyBeanMethods = false)
public class RegionModuleOutboxConfig {

    @Bean("regionOutboxEventSerializer")
    public OutboxEventSerializer regionOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("regionOutboxEventDispatcher")
    public OutboxEventDispatcher regionOutboxEventDispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("regionOutboxStore")
    public OutboxStore<RegionOutboxEvent, Long> regionOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(RegionOutboxEvent.class),
                RegionOutboxEvent.class
        );
    }

    @Bean("regionOutboxRelay")
    public OutboxRelay<Long> regionOutboxRelay(
            @Value("${region.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${region.outbox.hot-queue.workers:2}") int workers,
            @Value("${region.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${region.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("region", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("regionDomainEventProducer")
    public DomainEventProducer regionDomainEventProducer(
            @Qualifier("regionOutboxStore") OutboxStore<RegionOutboxEvent, Long> store,
            @Qualifier("regionOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("regionOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new RegionOutboxEventProducer(store, serializer, relay);
    }

    @Bean("regionSingletonJobExecutor")
    public SingletonJobExecutor regionSingletonJobExecutor(
            @Qualifier("channelDataSource") DataSource dataSource,
            @Value("${region.flyway.default-schema:region}") String schema,
            MeterRegistry meterRegistry
    ) {
        return ShedLockSingletonJobExecutor.forSchema(dataSource, schema, meterRegistry);
    }

    @Bean("regionOutboxEventProcessor")
    public RegionOutboxEventProcessor regionOutboxEventProcessor(
            @Qualifier("regionOutboxStore") OutboxStore<RegionOutboxEvent, Long> outboxStore,
            @Qualifier("regionOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("regionOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("regionTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("regionOutboxRelay") OutboxRelay<Long> relay,
            @Qualifier("regionSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            OutboxObserver outboxObserver,
            Environment environment
    ) {
        return new RegionOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                OutboxProcessorSettings.fromProperties(environment, "region.outbox"),
                relay,
                outboxObserver,
                singletonJobExecutor
        );
    }
}

