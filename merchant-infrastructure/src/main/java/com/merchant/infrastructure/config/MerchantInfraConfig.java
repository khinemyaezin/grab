package com.merchant.infrastructure.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.outbox.*;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.domain.repository.StorefrontChannelBrandRepository;
import com.merchant.domain.repository.StorefrontRepository;
import com.merchant.infrastructure.entity.StorefrontEntity;
import com.merchant.infrastructure.mapper.jpa.impl.MerchantAccountJpaAssembler;
import com.merchant.infrastructure.mapper.jpa.impl.StorefrontJpaAssembler;
import com.merchant.infrastructure.mapper.jpa.MerchantAccountEntityMapper;
import com.merchant.infrastructure.mapper.jpa.StorefrontChannelBrandEntityMapper;
import com.merchant.infrastructure.mapper.jpa.StorefrontChannelBrandJpaAssembler;
import com.merchant.infrastructure.mapper.jpa.StorefrontChannelBrandMapper;
import com.merchant.infrastructure.mapper.jpa.StorefrontEntityMapper;
import com.merchant.infrastructure.mapper.jpa.impl.StorefrontChannelBrandJpaAssemblerImpl;
import com.merchant.infrastructure.repository.jpa.StorefrontChannelBrandJpaRepository;
import com.merchant.infrastructure.repository.jpa.StorefrontJpaRepository;
import com.merchant.infrastructure.repository.jpa.StorefrontQueryRepository;
import com.merchant.infrastructure.repository.jpa.impl.DefaultStorefrontChannelBrandRepository;
import com.merchant.infrastructure.repository.jpa.impl.DefaultStorefrontQueryRepository;
import com.merchant.infrastructure.repository.jpa.impl.DefaultStorefrontRepository;
import com.merchant.infrastructure.specification.jpa.StorefrontQuerySpecification;
import com.merchant.infrastructure.outbox.*;
import com.merchant.infrastructure.repository.jpa.impl.DefaultMerchantAccountRepository;
import com.merchant.infrastructure.repository.jpa.impl.MerchantPersistenceExecutor;
import com.merchant.infrastructure.repository.jpa.MerchantAccountJpaRepository;
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
@Import(MerchantDomainConfig.class)
public class MerchantInfraConfig {
    @Bean("merchantOutboxEventSerializer")
    OutboxEventSerializer serializer() { return new JsonOutboxEventSerializer(); }

    @Bean("merchantOutboxEventDispatcher")
    OutboxEventDispatcher dispatcher(ApplicationEventPublisher publisher) { return publisher::publishEvent; }

    @Bean("merchantOutboxStore")
    OutboxStore<MerchantOutboxEvent, Long> outboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(MerchantOutboxEvent.class), MerchantOutboxEvent.class
        );
    }

    @Bean("merchantOutboxRelay")
    OutboxRelay<Long> merchantOutboxRelay(
            @Value("${merchant.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${merchant.outbox.hot-queue.workers:2}") int workers,
            @Value("${merchant.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${merchant.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("merchant", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("merchantDomainEventProducer")
    DomainEventProducer domainEventProducer(
            @Qualifier("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> store,
            @Qualifier("merchantOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("merchantOutboxRelay") OutboxRelay<Long> relay) {
        return new MerchantOutboxEventProducer(store, serializer, relay);
    }

    @Bean
    MerchantOutboxEventProcessor merchantOutboxEventProcessor(
            @Qualifier("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> store,
            @Qualifier("merchantOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("merchantOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("merchantTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${merchant.outbox.batch-size:20}") int batchSize,
            @Value("${merchant.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${merchant.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${merchant.outbox.retention-ms:604800000}") long retention,
            @Qualifier("merchantOutboxRelay") OutboxRelay<Long> relay) {
        return new MerchantOutboxEventProcessor(
                store, serializer, dispatcher, transactionManager, batchSize,
                Duration.ofMillis(retryDelay), Duration.ofMillis(claimTimeout), Duration.ofMillis(retention),
                relay
        );
    }

    @Bean("merchantPersistenceExecutor")
    PersistenceExecutor persistenceExecutor() { return new MerchantPersistenceExecutor(); }

    @Bean
    MerchantAccountJpaAssembler merchantAssembler(MerchantAccountEntityMapper entityMapper, IdMapper ids) {
        return new MerchantAccountJpaAssembler(entityMapper, ids);
    }

    @Bean
    MerchantAccountRepository merchantRepository(
            MerchantAccountJpaRepository merchants,
            MerchantAccountJpaAssembler assembler,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer events,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultMerchantAccountRepository(merchants, assembler, events, executor);
    }

    @Bean
    StorefrontJpaAssembler storefrontAssembler(StorefrontEntityMapper entityMapper, IdMapper ids) {
        return new StorefrontJpaAssembler(entityMapper, ids);
    }

    @Bean
    StorefrontRepository storefrontRepository(
            StorefrontJpaRepository storefronts,
            StorefrontJpaAssembler assembler,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer events,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultStorefrontRepository(storefronts, assembler, events, executor);
    }

    @Bean
    StorefrontChannelBrandJpaAssembler storefrontChannelBrandJpaAssembler(
            StorefrontChannelBrandEntityMapper entityMapper,
            StorefrontChannelBrandMapper domainMapper
    ) {
        return new StorefrontChannelBrandJpaAssemblerImpl(entityMapper, domainMapper);
    }

    @Bean
    StorefrontChannelBrandRepository storefrontChannelBrandRepository(
            StorefrontChannelBrandJpaRepository brands,
            StorefrontJpaRepository storefronts,
            StorefrontChannelBrandJpaAssembler assembler,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer events,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultStorefrontChannelBrandRepository(brands, storefronts, assembler, events, executor);
    }

    @Bean
    StorefrontQuerySpecification storefrontQuerySpecification(JpaContext context) {
        return new StorefrontQuerySpecification(
                context.getEntityManagerByManagedType(StorefrontEntity.class)
        );
    }

    @Bean
    StorefrontQueryRepository storefrontQueryRepository(
            StorefrontQuerySpecification specification,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultStorefrontQueryRepository(specification, executor);
    }
}
