package com.catalog.infrastructure.configuration;

import com.catalog.infrastructure.mapper.IdMapper;
import com.catalog.infrastructure.mapper.jpa.*;
import com.nestedset.app.NestedSetNodeRepository;
import com.nestedset.app.config.NestedSetRepositoryConfiguration;
import com.nestedset.app.config.factory.JpaNestedSetNodeRepositoryFactory;
import com.nestedset.app.service.TreeBuilderImpl;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.event.ApplicationDomainEventProducer;
import com.catalog.infrastructure.event.DomainEventProducer;
import com.catalog.infrastructure.factory.CategoryComponentFactory;
import com.catalog.infrastructure.mapper.jpa.impl.CategoryJpaAssemblerImpl;
import com.catalog.infrastructure.mapper.jpa.impl.ProductJpaAssemblerImpl;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import com.catalog.infrastructure.repository.jpa.ProductJpaRepo;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.repository.jpa.impl.CategoryJpaRepository;
import com.catalog.infrastructure.repository.jpa.impl.ProductQueryJpqlRepository;
import com.catalog.infrastructure.repository.jpa.impl.ProductJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaContext;

@Configuration
@Import(CatalogDomainConfig.class)
public class CatalogInfraConfig {
    @Bean
    public IdMapper idMapper() {
        return new IdMapper();
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

    @Bean
    public NestedSetRepositoryConfiguration<CategoryEntity,Long> categoryRepositoryConfiguration(JpaContext context) {
        return new NestedSetRepositoryConfiguration<>(
                context,
                CategoryEntity.class
        );
    }

    @Bean
    public NestedSetNodeRepository<CategoryEntity,Long> categoryNodeRepository(NestedSetRepositoryConfiguration<CategoryEntity,Long> configuration) {
        return JpaNestedSetNodeRepositoryFactory.create(
                configuration,
                new TreeBuilderImpl<>(new CategoryComponentFactory())
        );
    }

    @Bean
    public CategoryJpaAssembler categoryJpaAssembler(
            CategoryEntityMapper categoryEntityMapper,
            CategoryMapper categoryMapper) {
        return new CategoryJpaAssemblerImpl(
                categoryEntityMapper,
                categoryMapper
        );
    }

    @Bean
    public CategoryRepository categoryRepository(
            NestedSetNodeRepository<CategoryEntity,Long> nodeRepository,
            CategoryJpaRepo categoryJpaRepository,
            CategoryJpaAssembler categoryJpaAssembler,
            DomainEventProducer domainEventProducer) {
        return new CategoryJpaRepository(
                nodeRepository,
                categoryJpaRepository,
                categoryJpaAssembler,
                domainEventProducer);
    }
}