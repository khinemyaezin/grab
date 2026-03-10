package com.catalog.infrastructure.configuration;

import com.catalog.infrastructure.mapper.jpa.*;
import com.nestedset.app.NestedSetNodeRepository;
import com.nestedset.app.config.NestedSetRepositoryConfiguration;
import com.nestedset.app.config.factory.JpaNestedSetNodeRepositoryFactory;
import com.nestedset.app.service.TreeBuilderImpl;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.outbox.CatalogOutboxEvent;
import com.catalog.infrastructure.outbox.CatalogOutboxEventProcessor;
import com.catalog.infrastructure.outbox.CatalogOutboxEventProducer;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.OutboxStore;
import com.catalog.infrastructure.factory.CategoryComponentFactory;
import com.catalog.infrastructure.mapper.jpa.impl.CategoryJpaAssemblerImpl;
import com.catalog.infrastructure.mapper.jpa.impl.ProductJpaAssemblerImpl;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import com.catalog.infrastructure.repository.jpa.ProductJpaRepo;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.repository.jpa.impl.CategoryJpaRepository;
import com.catalog.infrastructure.repository.jpa.impl.ProductQueryJpqlRepository;
import com.catalog.infrastructure.repository.jpa.impl.ProductJpaRepository;
import com.catalog.infrastructure.repository.jpa.support.CatalogPersistenceExecutor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration
@Import(CatalogDomainConfig.class)
public class CatalogInfraConfig {

    @Bean("catalogOutboxEventSerializer")
    public OutboxEventSerializer catalogOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("catalogOutboxEventDispatcher")
    public OutboxEventDispatcher catalogOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("catalogDomainEventProducer")
    public DomainEventProducer catalogDomainEventProducer(
            @Qualifier("catalogOutboxStore") OutboxStore<CatalogOutboxEvent, Long> outboxStore,
            @Qualifier("catalogOutboxEventSerializer") OutboxEventSerializer serializer
    ) {
        return new CatalogOutboxEventProducer(outboxStore, serializer);
    }

    @Bean("catalogOutboxStore")
    public OutboxStore<CatalogOutboxEvent, Long> catalogOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(CatalogOutboxEvent.class),
                CatalogOutboxEvent.class
        );
    }

    @Bean("catalogPersistenceExecutor")
    public PersistenceExecutor catalogPersistenceExecutor() {
        return new CatalogPersistenceExecutor();
    }

    @Bean
    public CatalogOutboxEventProcessor catalogOutboxEventProcessor(
            @Qualifier("catalogOutboxStore") OutboxStore<CatalogOutboxEvent, Long> outboxStore,
            @Qualifier("catalogOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("catalogOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("catalogTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${catalog.outbox.batch-size:20}") int batchSize,
            @Value("${catalog.outbox.retry-delay-ms:30000}") long retryDelayMs,
            @Value("${catalog.outbox.claim-timeout-ms:120000}") long claimTimeoutMs,
            @Value("${catalog.outbox.retention-ms:604800000}") long retentionMs
    ) {
        return new CatalogOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                batchSize,
                Duration.ofMillis(retryDelayMs),
                Duration.ofMillis(claimTimeoutMs),
                Duration.ofMillis(retentionMs)
        );
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
            @Qualifier("catalogDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor) {
        return new ProductJpaRepository(
                jpaAssembler, productJpaRepo, domainEventProducer, executor
        );
    }

    @Bean
    public ProductQueryRepository productQueryRepository(
            JpaContext context,
            ProductSummaryMapper productSummaryMapper,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new ProductQueryJpqlRepository(
                context.getEntityManagerByManagedType(ProductEntity.class),
                productSummaryMapper,
                executor
        );
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
            @Qualifier("catalogDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor) {
        return new CategoryJpaRepository(
                nodeRepository,
                categoryJpaRepository,
                categoryJpaAssembler,
                domainEventProducer,
                executor);
    }
}
