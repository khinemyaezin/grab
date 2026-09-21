package com.region.adapter.persistence.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.region.adapter.persistence.adapter.RegionPersistenceExecutor;
import com.region.adapter.persistence.adapter.RegionQueryAdapter;
import com.region.adapter.persistence.outbox.RegionOutboxEvent;
import com.region.adapter.persistence.outbox.RegionOutboxEventProcessor;
import com.region.adapter.persistence.outbox.RegionOutboxEventProducer;
import com.region.adapter.persistence.repository.RegionJpaRepository;
import com.region.application.port.outbound.RegionQueryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration
public class RegionPersistenceConfig {

    @Bean("regionOutboxEventSerializer")
    OutboxEventSerializer regionOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("regionOutboxEventDispatcher")
    OutboxEventDispatcher regionOutboxEventDispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("regionOutboxStore")
    OutboxStore<RegionOutboxEvent, Long> regionOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(RegionOutboxEvent.class),
                RegionOutboxEvent.class
        );
    }

    @Bean("regionOutboxRelay")
    OutboxRelay<Long> regionOutboxRelay(
            @Value("${region.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${region.outbox.hot-queue.workers:2}") int workers,
            @Value("${region.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${region.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("region", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("regionDomainEventProducer")
    DomainEventProducer regionDomainEventProducer(
            @Qualifier("regionOutboxStore") OutboxStore<RegionOutboxEvent, Long> store,
            @Qualifier("regionOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("regionOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new RegionOutboxEventProducer(store, serializer, relay);
    }

    @Bean
    RegionOutboxEventProcessor regionOutboxEventProcessor(
            @Qualifier("regionOutboxStore") OutboxStore<RegionOutboxEvent, Long> store,
            @Qualifier("regionOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("regionOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("regionTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${region.outbox.batch-size:20}") int batchSize,
            @Value("${region.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${region.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${region.outbox.retention-ms:604800000}") long retention,
            @Qualifier("regionOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new RegionOutboxEventProcessor(
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

    @Bean("regionPersistenceExecutor")
    PersistenceExecutor regionPersistenceExecutor() {
        return new RegionPersistenceExecutor();
    }

    @Bean
    RegionQueryPort regionQueryPort(
            RegionJpaRepository regions,
            @Qualifier("regionPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new RegionQueryAdapter(regions, executor);
    }
}
