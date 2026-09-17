package com.saleschannel.infrastructure.config;

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
import com.saleschannel.domain.repository.SalesChannelRepository;
import com.saleschannel.infrastructure.entity.SalesChannelEntity;
import com.saleschannel.infrastructure.mapper.jpa.SalesChannelEntityMapper;
import com.saleschannel.infrastructure.mapper.jpa.impl.SalesChannelJpaAssembler;
import com.saleschannel.infrastructure.outbox.SalesChannelOutboxEvent;
import com.saleschannel.infrastructure.outbox.SalesChannelOutboxEventProcessor;
import com.saleschannel.infrastructure.outbox.SalesChannelOutboxEventProducer;
import com.saleschannel.infrastructure.repository.jpa.SalesChannelJpaRepository;
import com.saleschannel.infrastructure.repository.jpa.SalesChannelQueryRepository;
import com.saleschannel.infrastructure.repository.jpa.impl.DefaultSalesChannelQueryRepository;
import com.saleschannel.infrastructure.repository.jpa.impl.DefaultSalesChannelRepository;
import com.saleschannel.infrastructure.repository.jpa.impl.SalesChannelPersistenceExecutor;
import com.saleschannel.infrastructure.specification.jpa.SalesChannelQuerySpecification;
import com.saleschannel.infrastructure.workflow.SalesChannelWorkflowStepRunner;
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
@Import(SalesChannelDomainConfig.class)
public class SalesChannelInfraConfig {
    @Bean("salesChannelOutboxEventSerializer")
    OutboxEventSerializer serializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("salesChannelOutboxEventDispatcher")
    OutboxEventDispatcher dispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("salesChannelOutboxStore")
    OutboxStore<SalesChannelOutboxEvent, Long> outboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(SalesChannelOutboxEvent.class),
                SalesChannelOutboxEvent.class
        );
    }

    @Bean("salesChannelOutboxRelay")
    OutboxRelay<Long> salesChannelOutboxRelay(
            @Value("${saleschannel.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${saleschannel.outbox.hot-queue.workers:2}") int workers,
            @Value("${saleschannel.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${saleschannel.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("saleschannel", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("salesChannelDomainEventProducer")
    DomainEventProducer domainEventProducer(
            @Qualifier("salesChannelOutboxStore") OutboxStore<SalesChannelOutboxEvent, Long> store,
            @Qualifier("salesChannelOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("salesChannelOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new SalesChannelOutboxEventProducer(store, serializer, relay);
    }

    @Bean
    SalesChannelOutboxEventProcessor processor(
            @Qualifier("salesChannelOutboxStore") OutboxStore<SalesChannelOutboxEvent, Long> store,
            @Qualifier("salesChannelOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("salesChannelOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("salesChannelTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${saleschannel.outbox.batch-size:20}") int batchSize,
            @Value("${saleschannel.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${saleschannel.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${saleschannel.outbox.retention-ms:604800000}") long retention,
            @Qualifier("salesChannelOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new SalesChannelOutboxEventProcessor(
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

    @Bean("salesChannelPersistenceExecutor")
    PersistenceExecutor persistenceExecutor() {
        return new SalesChannelPersistenceExecutor();
    }

    @Bean
    public SalesChannelWorkflowStepRunner salesChannelWorkflowSignalEmitter(
            @Qualifier("salesChannelDomainEventProducer") DomainEventProducer producer,
            @Qualifier("salesChannelTransactionManager") PlatformTransactionManager transactionManager
    ) {
        return new SalesChannelWorkflowStepRunner(producer, transactionManager);
    }

    @Bean
    SalesChannelJpaAssembler salesChannelAssembler(SalesChannelEntityMapper entityMapper, IdMapper ids) {
        return new SalesChannelJpaAssembler(entityMapper, ids);
    }

    @Bean
    SalesChannelRepository salesChannelRepository(
            SalesChannelJpaRepository salesChannels,
            SalesChannelJpaAssembler assembler,
            @Qualifier("salesChannelDomainEventProducer") DomainEventProducer events,
            @Qualifier("salesChannelPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultSalesChannelRepository(salesChannels, assembler, events, executor);
    }

    @Bean
    SalesChannelQuerySpecification salesChannelQuerySpecification(JpaContext context) {
        return new SalesChannelQuerySpecification(
                context.getEntityManagerByManagedType(SalesChannelEntity.class)
        );
    }

    @Bean
    SalesChannelQueryRepository salesChannelQueryRepository(
            SalesChannelQuerySpecification specification,
            @Qualifier("salesChannelPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultSalesChannelQueryRepository(specification, executor);
    }
}
