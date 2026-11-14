package com.grab.app.merchandising.catalog.internal.config;

import java.time.Duration;
import com.grab.outbox.infrastructure.inbox.ProcessedEventRetention;
import com.grab.framework.inbox.ProcessedEventRepository;
import javax.sql.DataSource;
import org.springframework.core.env.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import com.grab.outbox.infrastructure.OutboxProcessorSettings;
import com.grab.outbox.infrastructure.OutboxObserver;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.app.shared.scheduling.ShedLockSingletonJobExecutor;
import com.catalog.adapter.persistence.outbox.CatalogOutboxEvent;
import com.catalog.adapter.persistence.outbox.CatalogOutboxEventProcessor;
import com.catalog.adapter.persistence.outbox.CatalogOutboxEventProducer;
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


@Configuration(proxyBeanMethods = false)
public class CatalogModuleOutboxConfig {

    @Bean("catalogOutboxEventSerializer")
    public OutboxEventSerializer catalogOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("catalogOutboxEventDispatcher")
    public OutboxEventDispatcher catalogOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("catalogOutboxStore")
    public OutboxStore<CatalogOutboxEvent, Long> catalogOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(CatalogOutboxEvent.class),
                CatalogOutboxEvent.class
        );
    }

    @Bean("catalogOutboxRelay")
    public OutboxRelay<Long> catalogOutboxRelay(
            @Value("${catalog.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${catalog.outbox.hot-queue.workers:2}") int workers,
            @Value("${catalog.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${catalog.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("catalog", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("catalogDomainEventProducer")
    public DomainEventProducer catalogDomainEventProducer(
            @Qualifier("catalogOutboxStore") OutboxStore<CatalogOutboxEvent, Long> outboxStore,
            @Qualifier("catalogOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("catalogOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CatalogOutboxEventProducer(outboxStore, serializer, relay);
    }

    @Bean("catalogSingletonJobExecutor")
    public SingletonJobExecutor catalogSingletonJobExecutor(
            @Qualifier("catalogDataSource") DataSource dataSource,
            @Value("${catalog.flyway.default-schema:catalog}") String schema,
            MeterRegistry meterRegistry
    ) {
        return ShedLockSingletonJobExecutor.forSchema(dataSource, schema, meterRegistry);
    }

    @Bean("catalogOutboxEventProcessor")
    public CatalogOutboxEventProcessor catalogOutboxEventProcessor(
            @Qualifier("catalogOutboxStore") OutboxStore<CatalogOutboxEvent, Long> outboxStore,
            @Qualifier("catalogOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("catalogOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("catalogTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("catalogOutboxRelay") OutboxRelay<Long> relay,
            @Qualifier("catalogSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            OutboxObserver outboxObserver,
            Environment environment
    ) {
        return new CatalogOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                OutboxProcessorSettings.fromProperties(environment, "catalog.outbox"),
                relay,
                outboxObserver,
                singletonJobExecutor
        );
    }

    @Bean("catalogProcessedEventRetention")
    public ProcessedEventRetention catalogProcessedEventRetention(
            @Qualifier("catalogProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("catalogTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("catalogSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            @Value("${catalog.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${catalog.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "catalog", processedEvents, transactionManager, singletonJobExecutor, retention, batchSize);
    }
}
