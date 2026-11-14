package com.grab.app.merchandising.pricing.internal.config;

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
import com.pricing.adapter.persistence.outbox.PricingOutboxEvent;
import com.pricing.adapter.persistence.outbox.PricingOutboxEventProcessor;
import com.pricing.adapter.persistence.outbox.PricingOutboxEventProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration(proxyBeanMethods = false)
public class PricingModuleOutboxConfig {

    @Bean("pricingOutboxEventSerializer")
    public OutboxEventSerializer pricingOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("pricingOutboxEventDispatcher")
    public OutboxEventDispatcher pricingOutboxEventDispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("pricingOutboxStore")
    public OutboxStore<PricingOutboxEvent, Long> pricingOutboxStore(JpaContext context, Environment environment) {
        OutboxClaimStrategy claimStrategy = OutboxClaimStrategies.fromProperties(environment, "pricing.outbox");
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(PricingOutboxEvent.class),
                PricingOutboxEvent.class,
                claimStrategy
        );
    }

    @Bean("pricingOutboxRelay")
    public OutboxRelay<Long> pricingOutboxRelay(
            @Value("${pricing.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${pricing.outbox.hot-queue.workers:2}") int workers,
            @Value("${pricing.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${pricing.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("pricing", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("pricingDomainEventProducer")
    public DomainEventProducer pricingDomainEventProducer(
            @Qualifier("pricingOutboxStore") OutboxStore<PricingOutboxEvent, Long> store,
            @Qualifier("pricingOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("pricingOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new PricingOutboxEventProducer(store, serializer, relay);
    }

    @Bean("pricingSingletonJobExecutor")
    public SingletonJobExecutor pricingSingletonJobExecutor(
            @Qualifier("catalogDataSource") DataSource dataSource,
            @Value("${pricing.flyway.default-schema:pricing}") String schema,
            MeterRegistry meterRegistry,
            Environment environment
    ) {
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "pricing.scheduling", meterRegistry);
    }

    @Bean("pricingOutboxEventProcessor")
    public PricingOutboxEventProcessor pricingOutboxEventProcessor(
            @Qualifier("pricingOutboxStore") OutboxStore<PricingOutboxEvent, Long> outboxStore,
            @Qualifier("pricingOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("pricingOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("pricingTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("pricingOutboxRelay") OutboxRelay<Long> relay,
            @Qualifier("pricingSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            OutboxObserver outboxObserver,
            Environment environment
    ) {
        return new PricingOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                OutboxProcessorSettings.fromProperties(environment, "pricing.outbox"),
                relay,
                outboxObserver,
                singletonJobExecutor
        );
    }

    @Bean("pricingProcessedEventRetention")
    public ProcessedEventRetention pricingProcessedEventRetention(
            @Qualifier("pricingProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("pricingTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("pricingSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            @Value("${pricing.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${pricing.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "pricing", processedEvents, transactionManager, singletonJobExecutor, retention, batchSize);
    }
}
