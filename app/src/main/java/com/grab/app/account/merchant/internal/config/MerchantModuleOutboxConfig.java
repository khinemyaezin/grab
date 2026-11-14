package com.grab.app.account.merchant.internal.config;

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
import com.merchant.adapter.persistence.outbox.MerchantOutboxEvent;
import com.merchant.adapter.persistence.outbox.MerchantOutboxEventProcessor;
import com.merchant.adapter.persistence.outbox.MerchantOutboxEventProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration(proxyBeanMethods = false)
public class MerchantModuleOutboxConfig {

    @Bean("merchantOutboxEventSerializer")
    public OutboxEventSerializer merchantOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("merchantOutboxEventDispatcher")
    public OutboxEventDispatcher merchantOutboxEventDispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("merchantOutboxStore")
    public OutboxStore<MerchantOutboxEvent, Long> merchantOutboxStore(JpaContext context, Environment environment) {
        OutboxClaimStrategy claimStrategy = OutboxClaimStrategies.fromProperties(environment, "merchant.outbox");
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(MerchantOutboxEvent.class),
                MerchantOutboxEvent.class,
                claimStrategy
        );
    }

    @Bean("merchantOutboxRelay")
    public OutboxRelay<Long> merchantOutboxRelay(
            @Value("${merchant.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${merchant.outbox.hot-queue.workers:2}") int workers,
            @Value("${merchant.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${merchant.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("merchant", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("merchantDomainEventProducer")
    public DomainEventProducer merchantDomainEventProducer(
            @Qualifier("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> store,
            @Qualifier("merchantOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("merchantOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new MerchantOutboxEventProducer(store, serializer, relay);
    }

    @Bean("merchantSingletonJobExecutor")
    public SingletonJobExecutor merchantSingletonJobExecutor(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${merchant.flyway.default-schema:merchant}") String schema,
            MeterRegistry meterRegistry,
            Environment environment
    ) {
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "merchant.scheduling", meterRegistry);
    }

    @Bean("merchantOutboxEventProcessor")
    public MerchantOutboxEventProcessor merchantOutboxEventProcessor(
            @Qualifier("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> outboxStore,
            @Qualifier("merchantOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("merchantOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("merchantTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("merchantOutboxRelay") OutboxRelay<Long> relay,
            @Qualifier("merchantSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            OutboxObserver outboxObserver,
            Environment environment
    ) {
        return new MerchantOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                OutboxProcessorSettings.fromProperties(environment, "merchant.outbox"),
                relay,
                outboxObserver,
                singletonJobExecutor
        );
    }

    @Bean("merchantProcessedEventRetention")
    public ProcessedEventRetention merchantProcessedEventRetention(
            @Qualifier("merchantProcessedEventRepository") ProcessedEventRepository processedEvents,
            @Qualifier("merchantTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("merchantSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor,
            @Value("${merchant.inbox.retention-ms:1209600000}") long retentionMs,
            @Value("${merchant.inbox.cleanup-batch-size:1000}") int batchSize
    ) {
        Duration retention = Duration.ofMillis(retentionMs);
        return new ProcessedEventRetention(
                "merchant", processedEvents, transactionManager, singletonJobExecutor, retention, batchSize);
    }
}
