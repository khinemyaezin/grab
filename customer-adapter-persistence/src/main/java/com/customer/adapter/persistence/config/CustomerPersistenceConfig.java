package com.customer.adapter.persistence.config;

import com.customer.adapter.persistence.adapter.CustomerPersistenceExecutor;
import com.customer.adapter.persistence.adapter.CustomerQueryAdapter;
import com.customer.adapter.persistence.adapter.CustomerRepositoryAdapter;
import com.customer.adapter.persistence.mapper.jpa.CustomerJpaAssembler;
import com.customer.adapter.persistence.repository.jpa.CustomerJpaRepository;
import com.customer.application.port.outbound.CustomerQueryPort;
import com.customer.domain.port.outbound.CustomerRepository;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.support.PersistenceExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CustomerPersistenceConfig {

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
