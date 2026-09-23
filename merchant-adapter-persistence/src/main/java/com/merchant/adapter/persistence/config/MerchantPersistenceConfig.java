package com.merchant.adapter.persistence.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.outbox.*;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import com.merchant.domain.port.outbound.StorefrontChannelBrandRepository;
import com.merchant.domain.port.outbound.StorefrontRepository;
import com.merchant.adapter.persistence.entity.StorefrontEntity;
import com.merchant.adapter.persistence.mapper.jpa.impl.MerchantAccountJpaAssembler;
import com.merchant.adapter.persistence.mapper.jpa.impl.MerchantMemberJpaAssembler;
import com.merchant.adapter.persistence.mapper.jpa.impl.StorefrontJpaAssembler;
import com.merchant.adapter.persistence.mapper.jpa.MerchantAccountEntityMapper;
import com.merchant.adapter.persistence.mapper.jpa.MerchantMemberEntityMapper;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontChannelBrandEntityMapper;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontChannelBrandJpaAssembler;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontChannelBrandMapper;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontEntityMapper;
import com.merchant.adapter.persistence.mapper.jpa.impl.StorefrontChannelBrandJpaAssemblerImpl;
import com.merchant.adapter.persistence.repository.jpa.MerchantMemberJpaRepository;
import com.merchant.adapter.persistence.repository.jpa.StorefrontChannelBrandJpaRepository;
import com.merchant.adapter.persistence.repository.jpa.StorefrontJpaRepository;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.application.port.outbound.MerchantMemberQueryPort;
import com.merchant.application.port.outbound.StorefrontQueryPort;
import com.merchant.adapter.persistence.adapter.MerchantAccountQueryAdapter;
import com.merchant.adapter.persistence.adapter.MerchantAccountRepositoryAdapter;
import com.merchant.adapter.persistence.adapter.MerchantMemberQueryAdapter;
import com.merchant.adapter.persistence.adapter.MerchantMemberRepositoryAdapter;
import com.merchant.adapter.persistence.adapter.MerchantPersistenceExecutor;
import com.merchant.adapter.persistence.adapter.StorefrontChannelBrandRepositoryAdapter;
import com.merchant.adapter.persistence.adapter.StorefrontQueryAdapter;
import com.merchant.adapter.persistence.adapter.StorefrontRepositoryAdapter;
import com.merchant.adapter.persistence.specification.jpa.StorefrontQuerySpecification;
import com.merchant.adapter.persistence.outbox.*;
import com.merchant.adapter.persistence.repository.jpa.MerchantAccountJpaRepository;
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
public class MerchantPersistenceConfig {
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
        return new MerchantAccountRepositoryAdapter(merchants, assembler, events, executor);
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
        return new StorefrontRepositoryAdapter(storefronts, assembler, events, executor);
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
        return new StorefrontChannelBrandRepositoryAdapter(brands, storefronts, assembler, events, executor);
    }

    @Bean
    StorefrontQuerySpecification storefrontQuerySpecification(JpaContext context) {
        return new StorefrontQuerySpecification(
                context.getEntityManagerByManagedType(StorefrontEntity.class)
        );
    }

    @Bean
    StorefrontQueryPort storefrontQueryPort(
            StorefrontQuerySpecification specification,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new StorefrontQueryAdapter(specification, executor);
    }

    @Bean
    MerchantAccountQueryPort merchantAccountQueryPort(
            MerchantAccountJpaRepository merchants,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new MerchantAccountQueryAdapter(merchants, executor);
    }

    @Bean
    MerchantMemberJpaAssembler merchantMemberAssembler(MerchantMemberEntityMapper entityMapper, IdMapper ids) {
        return new MerchantMemberJpaAssembler(entityMapper, ids);
    }

    @Bean
    MerchantMemberRepository merchantMemberRepository(
            MerchantMemberJpaRepository members,
            MerchantMemberJpaAssembler assembler,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer events,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new MerchantMemberRepositoryAdapter(members, assembler, events, executor);
    }

    @Bean
    MerchantMemberQueryPort merchantMemberQueryPort(
            MerchantMemberJpaRepository members,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new MerchantMemberQueryAdapter(members, executor);
    }
}
