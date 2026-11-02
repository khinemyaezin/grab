package com.cart.adapter.persistence.config;

import com.cart.application.port.outbound.CartQueryPort;
import com.cart.domain.port.outbound.CartRepository;
import com.cart.adapter.persistence.adapter.CartPersistenceExecutor;
import com.cart.adapter.persistence.adapter.CartQueryAdapter;
import com.cart.adapter.persistence.adapter.CartRepositoryAdapter;
import com.cart.adapter.persistence.mapper.jpa.CartJpaAssembler;
import com.cart.adapter.persistence.repository.jpa.CartJpaRepository;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.support.PersistenceExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CartPersistenceConfig {

    @Bean("cartPersistenceExecutor")
    PersistenceExecutor persistenceExecutor() {
        return new CartPersistenceExecutor();
    }

    @Bean
    CartJpaAssembler cartAssembler(IdMapper ids) {
        return new CartJpaAssembler(ids);
    }

    @Bean
    CartRepository cartRepository(
            CartJpaRepository carts,
            CartJpaAssembler assembler,
            @Qualifier("cartDomainEventProducer") DomainEventProducer events,
            @Qualifier("cartPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new CartRepositoryAdapter(carts, assembler, events, executor);
    }

    @Bean
    CartQueryPort cartQueryPort(
            CartJpaRepository carts,
            @Qualifier("cartPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new CartQueryAdapter(carts, executor);
    }
}
