package com.grab.app.channel.saleschannel.internal.config;

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
    public OutboxStore<SalesChannelOutboxEvent, Long> salesChannelOutboxStore(JpaContext context, Environment environment) {
        OutboxClaimStrategy claimStrategy = OutboxClaimStrategies.fromProperties(environment, "saleschannel.outbox");
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(SalesChannelOutboxEvent.class),
                SalesChannelOutboxEvent.class,
                claimStrategy
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

    @Bean("salesChannelSingletonJobExecutor")
    public SingletonJobExecutor salesChannelSingletonJobExecutor(
            @Qualifier("channelDataSource") DataSource dataSource,
            @Value("${saleschannel.flyway.default-schema:saleschannel}") String schema,
            MeterRegistry meterRegistry,
            Environment environment
    ) {
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "saleschannel.scheduling", meterRegistry);
    }

    @Bean("salesChannelOutboxEventProcessor")
    public SalesChannelOutboxEventProcessor salesChannelOutboxEventProcessor(
            @Qualifier("salesChannelOutboxStore") OutboxStore<SalesChannelOutboxEvent, Long> outboxStore,
            @Qualifier("salesChannelOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("salesChannelOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("salesChannelTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("salesChannelOutboxRelay") OutboxRelay<Long> relay,
            @Qualifier("salesChannelSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            OutboxObserver outboxObserver,
            Environment environment
    ) {
        return new SalesChannelOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                OutboxProcessorSettings.fromProperties(environment, "saleschannel.outbox"),
                relay,
                outboxObserver,
                singletonJobExecutor
        );
    }

    @Bean("salesChannelProcessedEventRetention")
    public ProcessedEventRetention salesChannelProcessedEventRetention(
            @Qualifier("salesChannelProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("salesChannelTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("salesChannelSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            @Value("${saleschannel.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${saleschannel.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "saleschannel", processedEvents, transactionManager, singletonJobExecutor, retention, batchSize);
    }
}
