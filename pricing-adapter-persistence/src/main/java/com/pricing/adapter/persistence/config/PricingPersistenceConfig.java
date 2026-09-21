package com.pricing.adapter.persistence.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.pricing.domain.port.outbound.PriceListRepository;
import com.pricing.domain.port.outbound.PricePreferenceRepository;
import com.pricing.domain.port.outbound.PriceSetRepository;
import com.pricing.adapter.persistence.mapper.jpa.VariantPriceSetLinkJpaAssembler;
import com.pricing.adapter.persistence.mapper.jpa.impl.PricingJpaAssembler;
import com.pricing.adapter.persistence.mapper.jpa.impl.VariantPriceSetLinkJpaAssemblerImpl;
import com.pricing.adapter.persistence.outbox.PricingOutboxEvent;
import com.pricing.adapter.persistence.outbox.PricingOutboxEventProcessor;
import com.pricing.adapter.persistence.outbox.PricingOutboxEventProducer;
import com.pricing.adapter.persistence.repository.jpa.PriceJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PriceListJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PricePreferenceJpaRepository;
import com.pricing.application.port.outbound.PriceQueryPort;
import com.pricing.adapter.persistence.repository.jpa.PriceSetJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.VariantPriceSetLinkJpaRepository;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import com.pricing.domain.port.outbound.VariantPriceSetLinkRepository;
import com.pricing.adapter.persistence.adapter.PriceListRepositoryAdapter;
import com.pricing.adapter.persistence.adapter.PricePreferenceRepositoryAdapter;
import com.pricing.adapter.persistence.adapter.PriceQueryAdapter;
import com.pricing.adapter.persistence.adapter.PriceSetRepositoryAdapter;
import com.pricing.adapter.persistence.adapter.PricingPersistenceExecutor;
import com.pricing.adapter.persistence.adapter.VariantPriceSetLinkQueryAdapter;
import com.pricing.adapter.persistence.adapter.VariantPriceSetLinkRepositoryAdapter;
import com.pricing.adapter.persistence.workflow.PricingWorkflowStepRunner;
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
public class PricingPersistenceConfig {

    @Bean("pricingOutboxEventSerializer")
    OutboxEventSerializer serializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("pricingOutboxEventDispatcher")
    OutboxEventDispatcher dispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("pricingOutboxStore")
    OutboxStore<PricingOutboxEvent, Long> outboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(PricingOutboxEvent.class),
                PricingOutboxEvent.class
        );
    }

    @Bean("pricingOutboxRelay")
    OutboxRelay<Long> pricingOutboxRelay(
            @Value("${pricing.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${pricing.outbox.hot-queue.workers:2}") int workers,
            @Value("${pricing.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${pricing.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("pricing", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("pricingDomainEventProducer")
    DomainEventProducer domainEventProducer(
            @Qualifier("pricingOutboxStore") OutboxStore<PricingOutboxEvent, Long> store,
            @Qualifier("pricingOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("pricingOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new PricingOutboxEventProducer(store, serializer, relay);
    }

    @Bean
    PricingOutboxEventProcessor pricingOutboxEventProcessor(
            @Qualifier("pricingOutboxStore") OutboxStore<PricingOutboxEvent, Long> store,
            @Qualifier("pricingOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("pricingOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("pricingTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${pricing.outbox.batch-size:20}") int batchSize,
            @Value("${pricing.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${pricing.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${pricing.outbox.retention-ms:604800000}") long retention,
            @Qualifier("pricingOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new PricingOutboxEventProcessor(
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

    @Bean
    public PricingWorkflowStepRunner pricingWorkflowSignalEmitter(
            @Qualifier("pricingDomainEventProducer") DomainEventProducer producer,
            @Qualifier("pricingTransactionManager") PlatformTransactionManager transactionManager
    ) {
        return new PricingWorkflowStepRunner(producer, transactionManager);
    }

    @Bean("pricingPersistenceExecutor")
    PersistenceExecutor persistenceExecutor() {
        return new PricingPersistenceExecutor();
    }

    @Bean
    PricingJpaAssembler pricingJpaAssembler(IdMapper idMapper, ObjectMapper objectMapper) {
        return new PricingJpaAssembler(idMapper, objectMapper);
    }

    @Bean
    PriceSetRepository priceSetRepository(
            PriceSetJpaRepository priceSets,
            PricingJpaAssembler assembler,
            @Qualifier("pricingDomainEventProducer") DomainEventProducer events,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new PriceSetRepositoryAdapter(priceSets, assembler, events, executor);
    }

    @Bean
    PriceListRepository priceListRepository(
            PriceListJpaRepository priceLists,
            PriceSetJpaRepository priceSets,
            PricingJpaAssembler assembler,
            @Qualifier("pricingDomainEventProducer") DomainEventProducer events,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new PriceListRepositoryAdapter(priceLists, priceSets, assembler, events, executor);
    }

    @Bean
    PricePreferenceRepository pricePreferenceRepository(
            PricePreferenceJpaRepository preferences,
            PricingJpaAssembler assembler,
            @Qualifier("pricingDomainEventProducer") DomainEventProducer events,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new PricePreferenceRepositoryAdapter(preferences, assembler, events, executor);
    }

    @Bean
    PriceQueryPort priceQueryPort(
            PriceJpaRepository prices,
            PriceSetJpaRepository priceSets,
            PriceListJpaRepository priceLists,
            PricePreferenceJpaRepository preferences,
            IdMapper idMapper,
            ObjectMapper objectMapper,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new PriceQueryAdapter(
                prices, priceSets, priceLists, preferences, idMapper, objectMapper, executor);
    }

    @Bean
    VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort(
            VariantPriceSetLinkJpaRepository links,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new VariantPriceSetLinkQueryAdapter(links, executor);
    }


    @Bean
    VariantPriceSetLinkJpaAssembler variantPriceSetLinkJpaAssembler() {
        return new VariantPriceSetLinkJpaAssemblerImpl();
    }

    @Bean
    VariantPriceSetLinkRepository variantPriceSetLinkRepository(
            VariantPriceSetLinkJpaRepository links,
            VariantPriceSetLinkJpaAssembler assembler,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new VariantPriceSetLinkRepositoryAdapter(links, assembler, executor);
    }
}
