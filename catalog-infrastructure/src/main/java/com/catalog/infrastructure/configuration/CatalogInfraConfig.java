package com.catalog.infrastructure.configuration;

import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.VariantTypeRepository;
import com.catalog.infrastructure.repository.jpa.CategoryHierarchyPort;
import com.catalog.infrastructure.adapter.category.CategoryNodeInserter;
import com.catalog.infrastructure.adapter.category.CategoryNodeRemover;
import com.catalog.infrastructure.adapter.category.CategoryNodeRetriever;
import com.catalog.infrastructure.adapter.category.impl.CategoryNodeInserterImpl;
import com.catalog.infrastructure.adapter.category.impl.CategoryNodeRemoverImpl;
import com.catalog.infrastructure.adapter.category.impl.CategoryNodeRetrieverImpl;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.factory.CategoryComponentFactory;
import com.catalog.infrastructure.mapper.jpa.*;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.mapper.jpa.impl.CategoryJpaAssemblerImpl;
import com.catalog.infrastructure.mapper.jpa.impl.VariantTypeJpaAssemblerImpl;
import com.catalog.infrastructure.outbox.CatalogOutboxEvent;
import com.catalog.infrastructure.outbox.CatalogOutboxEventProcessor;
import com.catalog.infrastructure.outbox.CatalogOutboxEventProducer;
import com.catalog.infrastructure.repository.jpa.*;
import com.catalog.infrastructure.repository.jpa.adapter.*;
import com.catalog.infrastructure.repository.jpa.impl.*;
import com.catalog.infrastructure.repository.jpa.impl.VariantOptionQueryRepositoryImpl;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.OutboxStore;
import com.catalog.infrastructure.mapper.jpa.impl.ProductJpaAssemblerImpl;
import com.catalog.infrastructure.repository.jpa.impl.CatalogPersistenceExecutor;
import com.nestedset.app.config.JpaNestedSetRepositoryConfiguration;
import com.nestedset.app.service.TreeBuilder;
import com.nestedset.app.service.TreeBuilderImpl;
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
        return new ProductRepositoryImpl(
                jpaAssembler, productJpaRepo, domainEventProducer, executor
        );
    }

    @Bean
    public VariantTypeJpaAssembler variantTypeJpaAssembler(IdGenerator idGenerator) {
        return new VariantTypeJpaAssemblerImpl(idGenerator);
    }

    @Bean
    public VariantTypeRepository variantTypeRepository(
            VariantTypeJpaAssembler variantTypeJpaAssembler,
            VariantTypeJpaRepo variantTypeJpaRepo,
            @Qualifier("catalogDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new VariantTypeRepositoryImpl(
                variantTypeJpaAssembler,
                variantTypeJpaRepo,
                domainEventProducer,
                executor
        );
    }

    @Bean
    public ProductQueryRepository productQueryRepository(
            JpaContext context,
            ProductSummaryMapper productSummaryMapper,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new ProductQueryRepositoryImpl(
                context.getEntityManagerByManagedType(ProductEntity.class),
                productSummaryMapper,
                executor
        );
    }

    @Bean
    public JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> categoryNestedSetConfig(JpaContext jpaContext) {
        return new JpaNestedSetRepositoryConfiguration<>(jpaContext, CategoryEntity.class);
    }

    @Bean
    public CategoryComponentFactory categoryComponentFactory() {
        return new CategoryComponentFactory();
    }

    @Bean
    public TreeBuilder<CategoryEntity, Long> categoryTreeBuilder(CategoryComponentFactory categoryComponentFactory) {
        return new TreeBuilderImpl<>(categoryComponentFactory);
    }

    @Bean
    public CategoryJpaInsertingDelegate categoryJpaInsertingDelegate(
            JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config
    ) {
        return new CategoryJpaInsertingDelegateImpl(config);
    }

    @Bean
    public CategoryJpaRemovingDelegate categoryJpaRemovingDelegate(
            JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config
    ) {
        return new CategoryJpaRemovingDelegateImpl(config);
    }

    @Bean
    public CategoryJpaRetrievingDelegate categoryJpaRetrievingDelegate(
            JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config
    ) {
        return new CategoryJpaRetrievingDelegateImpl(config);
    }

    @Bean
    public CategoryNodeInserter categoryNodeInserter(CategoryJpaInsertingDelegate insertingDelegate) {
        return new CategoryNodeInserterImpl(insertingDelegate);
    }

    @Bean
    public CategoryNodeRemover categoryNodeRemover(CategoryJpaRemovingDelegate removingDelegate) {
        return new CategoryNodeRemoverImpl(removingDelegate);
    }

    @Bean
    public CategoryNodeRetriever categoryNodeRetriever(CategoryJpaRetrievingDelegate retrievingDelegate) {
        return new CategoryNodeRetrieverImpl(retrievingDelegate);
    }

    @Bean
    public CategoryJpaAssembler  categoryJpaAssembler(CategoryEntityMapper categoryEntityMapper,
                                                      CategoryMapper categoryMapper
    ) {
        return new CategoryJpaAssemblerImpl(categoryEntityMapper, categoryMapper);
    }

    @Bean
    public CategoryNestedSetNodeRepository categoryNestedSetNodeRepository(
            CategoryNodeInserter categoryNodeInserter,
            CategoryNodeRemover categoryNodeRemover,
            CategoryNodeRetriever categoryNodeRetriever,
            TreeBuilder<CategoryEntity, Long> categoryTreeBuilder,
            CategoryJpaRetrievingDelegate categoryJpaRetrievingDelegate
    ) {
        return new CategoryNestedSetNodeRepositoryImpl(
                categoryNodeInserter,
                categoryNodeRemover,
                categoryNodeRetriever,
                categoryTreeBuilder,
                categoryJpaRetrievingDelegate
        );
    }

    @Bean
    public CategoryNodeRepository categoryNodeRepository(
            CategoryJpaRepo categoryJpaRepository,
            CategoryNestedSetNodeRepository categoryNestedSetNodeRepository) {
        return new CategoryNodeRepositoryImpl(categoryJpaRepository,categoryNestedSetNodeRepository);
    }

    @Bean
    public CategoryRepository categoryRepository(
            CategoryNodeRepository categoryNodeRepository,
            CategoryJpaRepo categoryJpaRepository,
            CategoryJpaAssembler categoryJpaAssembler,
            @Qualifier("catalogDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor) {
        return new CategoryJpaRepositoryImpl(
                categoryNodeRepository,
                categoryJpaRepository,
                categoryJpaAssembler,
                domainEventProducer,
                executor);
    }

    @Bean
    public CategoryHierarchyPort categoryHierarchyPort(
            CategoryNodeRepository categoryNodeRepository,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor,
            IdGenerator idGenerator
    ) {
        return new CategoryHierarchyJpaRepository(categoryNodeRepository, executor, idGenerator);
    }

    @Bean
    public CategoryQueryRepository categoryQueryRepository(
            CategoryJpaRepo categoryJpaRepo,
            CategoryNodeRepository categoryNodeRepository,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new CategoryQueryRepositoryImpl(categoryJpaRepo, categoryNodeRepository, executor);
    }

    @Bean
    public VariantOptionQueryRepository variantOptionQueryRepository(
            VariantOptionJpaRepo variantOptionQueryJpaRepo,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new VariantOptionQueryRepositoryImpl(variantOptionQueryJpaRepo, executor);
    }

    @Bean
    public VariantTypeQueryRepository variantTypeQueryRepository(
            VariantTypeJpaRepo variantTypeJpaRepo,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new VariantTypeQueryRepositoryImpl(variantTypeJpaRepo, executor);
    }
}
