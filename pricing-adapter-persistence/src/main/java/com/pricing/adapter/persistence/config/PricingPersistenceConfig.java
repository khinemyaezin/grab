package com.pricing.adapter.persistence.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.support.PersistenceExecutor;
import com.pricing.domain.port.outbound.PriceListRepository;
import com.pricing.domain.port.outbound.PricePreferenceRepository;
import com.pricing.domain.port.outbound.PriceSetRepository;
import com.pricing.adapter.persistence.mapper.jpa.VariantPriceSetLinkJpaAssembler;
import com.pricing.adapter.persistence.mapper.jpa.impl.PricingJpaAssembler;
import com.pricing.adapter.persistence.mapper.jpa.impl.VariantPriceSetLinkJpaAssemblerImpl;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PricingPersistenceConfig {

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
