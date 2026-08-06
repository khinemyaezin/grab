package com.pricing.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.pricing.domain.repository.PriceListRepository;
import com.pricing.domain.repository.PricePreferenceRepository;
import com.pricing.domain.repository.PriceSetRepository;
import com.pricing.infrastructure.mapper.jpa.impl.PricingJpaAssembler;
import com.pricing.infrastructure.outbox.PricingOutboxEvent;
import com.pricing.infrastructure.outbox.PricingOutboxEventProcessor;
import com.pricing.infrastructure.outbox.PricingOutboxEventProducer;
import com.pricing.infrastructure.repository.jpa.PriceJpaRepository;
import com.pricing.infrastructure.repository.jpa.PriceListJpaRepository;
import com.pricing.infrastructure.repository.jpa.PricePreferenceJpaRepository;
import com.pricing.infrastructure.repository.jpa.PriceQueryRepository;
import com.pricing.infrastructure.repository.jpa.PriceSetJpaRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkJpaRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkQueryRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkRepository;
import com.pricing.infrastructure.repository.jpa.impl.DefaultPriceListRepository;
import com.pricing.infrastructure.repository.jpa.impl.DefaultPricePreferenceRepository;
import com.pricing.infrastructure.repository.jpa.impl.DefaultPriceQueryRepository;
import com.pricing.infrastructure.repository.jpa.impl.DefaultPriceSetRepository;
import com.pricing.infrastructure.repository.jpa.impl.DefaultVariantPriceSetLinkQueryRepository;
import com.pricing.infrastructure.repository.jpa.impl.DefaultVariantPriceSetLinkRepository;
import com.pricing.infrastructure.repository.jpa.impl.PricingPersistenceExecutor;
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
@Import(PricingDomainConfig.class)
public class PricingInfraConfig {

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

    @Bean("pricingDomainEventProducer")
    DomainEventProducer domainEventProducer(
            @Qualifier("pricingOutboxStore") OutboxStore<PricingOutboxEvent, Long> store,
            @Qualifier("pricingOutboxEventSerializer") OutboxEventSerializer serializer
    ) {
        return new PricingOutboxEventProducer(store, serializer);
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
            @Value("${pricing.outbox.retention-ms:604800000}") long retention
    ) {
        return new PricingOutboxEventProcessor(
                store,
                serializer,
                dispatcher,
                transactionManager,
                batchSize,
                Duration.ofMillis(retryDelay),
                Duration.ofMillis(claimTimeout),
                Duration.ofMillis(retention)
        );
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
        return new DefaultPriceSetRepository(priceSets, assembler, events, executor);
    }

    @Bean
    PriceListRepository priceListRepository(
            PriceListJpaRepository priceLists,
            PriceSetJpaRepository priceSets,
            PricingJpaAssembler assembler,
            @Qualifier("pricingDomainEventProducer") DomainEventProducer events,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultPriceListRepository(priceLists, priceSets, assembler, events, executor);
    }

    @Bean
    PricePreferenceRepository pricePreferenceRepository(
            PricePreferenceJpaRepository preferences,
            PricingJpaAssembler assembler,
            @Qualifier("pricingDomainEventProducer") DomainEventProducer events,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultPricePreferenceRepository(preferences, assembler, events, executor);
    }

    @Bean
    PriceQueryRepository priceQueryRepository(
            PriceJpaRepository prices,
            PricePreferenceJpaRepository preferences,
            IdMapper idMapper,
            ObjectMapper objectMapper,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultPriceQueryRepository(prices, preferences, idMapper, objectMapper, executor);
    }

    @Bean
    VariantPriceSetLinkQueryRepository variantPriceSetLinkQueryRepository(
            VariantPriceSetLinkJpaRepository links,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultVariantPriceSetLinkQueryRepository(links, executor);
    }

    @Bean
    VariantPriceSetLinkRepository variantPriceSetLinkWriteRepository(
            VariantPriceSetLinkJpaRepository links,
            @Qualifier("pricingPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultVariantPriceSetLinkRepository(links, executor);
    }
}
