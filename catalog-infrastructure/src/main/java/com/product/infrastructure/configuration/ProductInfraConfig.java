package com.product.infrastructure.configuration;

import com.grab.framework.mapper.CommonMapper;
import com.product.domain.repository.ProductRepository;
import com.product.infrastructure.event.ApplicationDomainEventProducer;
import com.product.infrastructure.event.DomainEventProducer;
import com.product.infrastructure.mapper.jpa.*;
import com.product.infrastructure.mapper.jpa.impl.ProductJpaAssemblerImpl;
import com.product.infrastructure.repository.jpa.ProductJpaRepo;
import com.product.infrastructure.repository.jpa.ProductQueryRepository;
import com.product.infrastructure.repository.jpa.impl.ProductQueryJpqlRepository;
import com.product.infrastructure.repository.jpa.impl.ProductJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DomainConfig.class)
public class ProductInfraConfig {
    @Bean
    public CommonMapper commonMapper() {
        return new CommonMapper();
    }

    @Bean
    public DomainEventProducer domainEventProducer(ApplicationEventPublisher applicationEventPublisher) {
        return new ApplicationDomainEventProducer(applicationEventPublisher);
    }

    @Bean
    public ProductJpaAssembler productJpaAssembler(
            ProductEntityMapper productEntityMapper,
    ProductVariantEntityMapper variantEntityMapper,
    ProductMapper productMapper,
    ProductVariantMapper productVariantMapper,
    ProductVariationMapper productVariationMapper
    ) {
        return new ProductJpaAssemblerImpl(productEntityMapper,variantEntityMapper,productMapper,productVariantMapper,productVariationMapper);
    }

    @Bean
    public ProductRepository productRepository(
            ProductJpaAssembler jpaAssembler,
            ProductJpaRepo productJpaRepo,
            DomainEventProducer domainEventProducer) {
        return new ProductJpaRepository(
                jpaAssembler, productJpaRepo, domainEventProducer
        );
    }

    @Bean
    public ProductQueryRepository productQueryRepository(
            EntityManager entityManager,
            ProductSummaryMapper productSummaryMapper
    ) {
        return new ProductQueryJpqlRepository(entityManager, productSummaryMapper);
    }
}