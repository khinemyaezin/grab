package com.grab.outbox.infrastructure.module;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.outbox.infrastructure.adapter.in.relay.OutboxRelays;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxDomainEventProducer;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.adapter.out.jpa.OutboxRowFactory;
import com.grab.outbox.infrastructure.adapter.out.jpa.claim.OutboxClaimStrategies;
import com.grab.outbox.infrastructure.adapter.out.jpa.claim.OutboxClaimStrategy;
import com.grab.outbox.infrastructure.adapter.out.jpa.claim.OutboxClaimStrategyCheck;
import com.grab.outbox.infrastructure.adapter.out.micrometer.MicrometerOutboxObserver;
import com.grab.outbox.infrastructure.adapter.out.micrometer.OutboxBacklogGauges;
import com.grab.outbox.infrastructure.adapter.out.spring.InProcessOutboxEventDispatcher;
import com.grab.outbox.infrastructure.adapter.out.spring.SpringOutboxTransactions;
import com.grab.outbox.infrastructure.core.port.in.DeliverOutboxEventsUseCase;
import com.grab.outbox.infrastructure.core.port.in.OutboxAdministrationPort;
import com.grab.outbox.infrastructure.core.port.out.OutboxObserver;
import com.grab.outbox.infrastructure.core.port.out.OutboxTransactions;
import com.grab.outbox.infrastructure.core.service.OutboxAdministrationService;
import com.grab.outbox.infrastructure.core.service.OutboxDeliveryEngine;
import com.grab.outbox.infrastructure.core.service.OutboxOutcomeRecorder;
import com.grab.outbox.infrastructure.core.service.OutboxRetentionCleaner;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.PropertyResolver;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.function.LongSupplier;

public final class OutboxModule<T extends OutboxEntry<Long>> implements SmartLifecycle {

    private final OutboxRelay<Long> relay;
    private final OutboxDeliveryEngine<T, Long> delivery;
    private final OutboxAdministrationPort administration;
    private final DomainEventProducer producer;
    private final SingletonJob cleanupJob;
    private final LongSupplier dropped;
    private volatile boolean running;

    private OutboxModule(Builder<T> builder) {
        String module = builder.module;
        String prefix = module + ".outbox";
        PropertyResolver properties = Objects.requireNonNull(builder.properties, "properties");
        OutboxProcessorSettings settings = OutboxProcessorSettings.fromProperties(properties, prefix);
        Clock clock = Clock.systemUTC();
        OutboxClaimStrategy strategy = OutboxClaimStrategies.fromProperties(properties, prefix);
        JpaOutboxStore<T, Long> store = new JpaOutboxStore<>(builder.entityManager, builder.entityType, strategy);
        String fallback = properties.getProperty("outbox.claim-strategy-check", "fail");
        String mode = properties.getProperty(prefix + ".claim-strategy-check", fallback);
        OutboxClaimStrategyCheck.verify(module + "OutboxStore", store, OutboxClaimStrategyCheck.Mode.parse(mode));

        OutboxTransactions transactions = new SpringOutboxTransactions(builder.transactionManager);
        OutboxEventSerializer serializer = new JsonOutboxEventSerializer();
        relay = OutboxRelays.moduleRelay(module,
                properties.getProperty(prefix + ".hot-queue.enabled", Boolean.class, true),
                properties.getProperty(prefix + ".hot-queue.workers", Integer.class, 2),
                properties.getProperty(prefix + ".hot-queue.capacity", Integer.class, 1000),
                properties.getProperty(prefix + ".hot-queue.drain-timeout-ms", Long.class, 5000L));

        OutboxObserver observer = builder.meterRegistry == null ? OutboxObserver.NOOP
                : new MicrometerOutboxObserver(builder.meterRegistry);
        OutboxOutcomeRecorder<T, Long> outcomes = new OutboxOutcomeRecorder<>(module, store, transactions,
                settings.retryPolicy(), observer, settings.claimTimeout(), clock);
        OutboxRetentionCleaner cleaner = new OutboxRetentionCleaner(module, store, transactions, observer,
                settings.retention(), settings.cleanupBatchSize(), clock);
        delivery = new OutboxDeliveryEngine<>(store, serializer, builder.dispatcher, transactions, relay, outcomes,
                cleaner::cleanupPublished, settings.batchSize(), settings.claimTimeout(), clock);
        administration = new OutboxAdministrationService<>(module, store, transactions, clock);
        producer = new JpaOutboxDomainEventProducer<>(module, store, serializer, builder.rowFactory, relay, clock);
        cleanupJob = new SingletonJob(prefix + ".cleanup", settings.cleanupLockAtMostFor(),
                settings.cleanupLockAtLeastFor());
        OutboxRelays.bind(relay, delivery);

        dropped = relay::hotQueueDropped;
        if (builder.meterRegistry != null) {
            long globalTtl = properties.getProperty("outbox.metrics.backlog-cache-ttl-ms", Long.class, 15000L);
            long ttl = properties.getProperty(prefix + ".metrics.backlog-cache-ttl-ms", Long.class, globalTtl);
            OutboxBacklogGauges gauges = new OutboxBacklogGauges(module, store, transactions, dropped,
                    Duration.ofMillis(ttl), clock);
            gauges.bindTo(builder.meterRegistry);
        }
    }

    public static <T extends OutboxEntry<Long>> Builder<T> builder(
            String module, Class<T> entityType, OutboxRowFactory<T> rowFactory) {
        return new Builder<>(module, entityType, rowFactory);
    }

    public DomainEventProducer producer() {
        return producer;
    }

    public DeliverOutboxEventsUseCase<Long> delivery() {
        return delivery;
    }

    public OutboxAdministrationPort administration() {
        return administration;
    }

    public SingletonJob cleanupJob() {
        return cleanupJob;
    }

    @Override
    public int getPhase() {
        return DEFAULT_PHASE - 1;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public synchronized void start() {
        if (!running) {
            relay.start();
            running = true;
        }
    }

    @Override
    public synchronized void stop() {
        if (running) {
            delivery.shutdown();
            running = false;
        }
    }

    public static final class Builder<T extends OutboxEntry<Long>> {

        private final String module;
        private final Class<T> entityType;
        private final OutboxRowFactory<T> rowFactory;
        private EntityManager entityManager;
        private PlatformTransactionManager transactionManager;
        private OutboxEventDispatcher dispatcher;
        private PropertyResolver properties;
        private MeterRegistry meterRegistry;

        private Builder(String module, Class<T> entityType, OutboxRowFactory<T> rowFactory) {
            this.module = Objects.requireNonNull(module, "module");
            this.entityType = Objects.requireNonNull(entityType, "entityType");
            this.rowFactory = Objects.requireNonNull(rowFactory, "rowFactory");
        }

        public Builder<T> entityManager(EntityManager value) {
            entityManager = Objects.requireNonNull(value, "entityManager");
            return this;
        }

        public Builder<T> transactionManager(PlatformTransactionManager value) {
            transactionManager = Objects.requireNonNull(value, "transactionManager");
            return this;
        }

        public Builder<T> dispatcher(OutboxEventDispatcher value) {
            dispatcher = Objects.requireNonNull(value, "dispatcher");
            return this;
        }

        public Builder<T> eventPublisher(ApplicationEventPublisher publisher) {
            return dispatcher(new InProcessOutboxEventDispatcher(publisher));
        }

        public Builder<T> properties(PropertyResolver value) {
            properties = Objects.requireNonNull(value, "properties");
            return this;
        }

        public Builder<T> meterRegistry(MeterRegistry value) {
            meterRegistry = value;
            return this;
        }

        public OutboxModule<T> build() {
            Objects.requireNonNull(transactionManager, "transactionManager");
            Objects.requireNonNull(dispatcher, "dispatcher");
            return new OutboxModule<>(this);
        }
    }
}
