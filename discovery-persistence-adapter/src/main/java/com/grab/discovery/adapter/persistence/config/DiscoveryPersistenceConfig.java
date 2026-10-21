package com.grab.discovery.adapter.persistence.config;

import com.grab.discovery.adapter.persistence.adapter.DiscoveryPersistenceExecutor;
import com.grab.discovery.adapter.persistence.adapter.DiscoveryProductQueryAdapter;
import com.grab.discovery.adapter.persistence.adapter.DiscoveryProductRepositoryAdapter;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryProductEntityMapper;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductJpaRepository;
import com.grab.discovery.adapter.persistence.specification.jpa.DiscoveryProductSearchSpecification;
import com.grab.discovery.application.port.outbound.DiscoveryProductQueryPort;
import com.grab.discovery.application.port.outbound.DiscoveryProductRepository;
import com.grab.framework.support.PersistenceExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscoveryPersistenceConfig {

    @Bean("discoveryPersistenceExecutor")
    public PersistenceExecutor discoveryPersistenceExecutor() {
        return new DiscoveryPersistenceExecutor();
    }

    @Bean
    public DiscoveryProductSearchSpecification discoveryProductSearchSpecification() {
        return new DiscoveryProductSearchSpecification();
    }

    @Bean
    public DiscoveryProductQueryPort discoveryProductQueryPort(
            DiscoveryProductJpaRepository products,
            DiscoveryProductSearchSpecification searchSpecification,
            DiscoveryProductEntityMapper mapper,
            @Qualifier("discoveryPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DiscoveryProductQueryAdapter(products, searchSpecification, mapper, executor);
    }

    @Bean
    public DiscoveryProductRepository discoveryProductRepository(
            DiscoveryProductJpaRepository products,
            DiscoveryProductEntityMapper mapper,
            @Qualifier("discoveryPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DiscoveryProductRepositoryAdapter(products, mapper, executor);
    }
}
