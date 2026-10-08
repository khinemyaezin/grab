package com.customer.adapter.persistence.config;

import com.customer.adapter.persistence.adapter.CustomerPersistenceExecutor;
import com.customer.adapter.persistence.adapter.CustomerQueryAdapter;
import com.customer.adapter.persistence.adapter.CustomerRepositoryAdapter;
import com.customer.adapter.persistence.mapper.jpa.CustomerJpaAssembler;
import com.customer.adapter.persistence.outbox.CustomerOutboxEvent;
import com.customer.adapter.persistence.outbox.CustomerOutboxEventProcessor;
import com.customer.adapter.persistence.outbox.CustomerOutboxEventProducer;
import com.customer.adapter.persistence.repository.jpa.CustomerJpaRepository;
import com.customer.application.port.outbound.CustomerQueryPort;
import com.customer.domain.port.outbound.CustomerRepository;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration
public class CustomerPersistenceConfig {
    @Bean("customerOutboxEventSerializer")
    OutboxEventSerializer serializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("customerOutboxEventDispatcher")
    OutboxEventDispatcher dispatcher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }

    @Bean("customerOutboxStore")
    OutboxStore<CustomerOutboxEvent, Long> outboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(CustomerOutboxEvent.class),
                CustomerOutboxEvent.class
        );
    }

    @Bean("customerOutboxRelay")
    OutboxRelay<Long> customerOutboxRelay(
            @Value("${customer.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${customer.outbox.hot-queue.workers:2}") int workers,
            @Value("${customer.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${customer.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("customer", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("customerDomainEventProducer")
    DomainEventProducer domainEventProducer(
            @Qualifier("customerOutboxStore") OutboxStore<CustomerOutboxEvent, Long> store,
            @Qualifier("customerOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("customerOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CustomerOutboxEventProducer(store, serializer, relay);
    }

    @Bean
    CustomerOutboxEventProcessor processor(
            @Qualifier("customerOutboxStore") OutboxStore<CustomerOutboxEvent, Long> store,
            @Qualifier("customerOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("customerOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("customerTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${customer.outbox.batch-size:20}") int batchSize,
            @Value("${customer.outbox.retry-delay-ms:30000}") long retryDelay,
            @Value("${customer.outbox.claim-timeout-ms:120000}") long claimTimeout,
            @Value("${customer.outbox.retention-ms:604800000}") long retention,
            @Qualifier("customerOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CustomerOutboxEventProcessor(
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

    @Bean("customerPersistenceExecutor")
    PersistenceExecutor persistenceExecutor() {
        return new CustomerPersistenceExecutor();
    }

    @Bean
    CustomerJpaAssembler customerAssembler(IdMapper ids) {
        return new CustomerJpaAssembler(ids);
    }

    @Bean
    CustomerRepository customerRepository(
            CustomerJpaRepository customers,
            CustomerJpaAssembler assembler,
            @Qualifier("customerDomainEventProducer") DomainEventProducer events,
            @Qualifier("customerPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new CustomerRepositoryAdapter(customers, assembler, events, executor);
    }

    @Bean
    CustomerQueryPort customerQueryPort(
            CustomerJpaRepository customers,
            @Qualifier("customerPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new CustomerQueryAdapter(customers, executor);
    }

    @Bean
    public com.customer.adapter.persistence.workflow.CustomerWorkflowStepRunner customerWorkflowSignalEmitter(
            @Qualifier("customerDomainEventProducer") DomainEventProducer producer,
            @Qualifier("customerTransactionManager") PlatformTransactionManager transactionManager
    ) {
        return new com.customer.adapter.persistence.workflow.CustomerWorkflowStepRunner(producer, transactionManager);
    }
}
