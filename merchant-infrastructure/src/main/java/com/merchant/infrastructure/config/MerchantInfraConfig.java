package com.merchant.infrastructure.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.outbox.*;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.infrastructure.mapper.jpa.impl.MerchantAccountJpaAssembler;
import com.merchant.infrastructure.mapper.jpa.MerchantAccountEntityMapper;
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

    @Bean("merchantDomainEventProducer")
    DomainEventProducer domainEventProducer(
            @Qualifier("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> store,
            @Qualifier("merchantOutboxEventSerializer") OutboxEventSerializer serializer) {
        return new MerchantOutboxEventProducer(store, serializer);
    }

    @Bean
    MerchantOutboxEventProcessor processor(
            @Qualifier("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> store,
            @Qualifier("merchantOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("merchantOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("merchantTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${merchant.outbox.batch-size:20}") int batchSize,
            @Value("${merchant.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${merchant.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${merchant.outbox.retention-ms:604800000}") long retention) {
        return new MerchantOutboxEventProcessor(
                store, serializer, dispatcher, transactionManager, batchSize,
                Duration.ofMillis(retryDelay), Duration.ofMillis(claimTimeout), Duration.ofMillis(retention)
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
}
