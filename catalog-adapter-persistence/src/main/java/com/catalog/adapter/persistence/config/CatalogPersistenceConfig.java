package com.catalog.adapter.persistence.config;

import com.catalog.domain.port.outbound.CategoryRepository;
import com.catalog.domain.port.outbound.VariantTypeRepository;
import com.catalog.application.port.outbound.CategoryHierarchyPort;
import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.port.outbound.MerchantAvailabilityPort;
import com.catalog.application.port.outbound.ProductAuditPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.port.outbound.ProductVariantQueryPort;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import com.catalog.application.port.outbound.VariantTypeQueryPort;
import com.catalog.adapter.persistence.category.CategoryNodeInserter;
import com.catalog.adapter.persistence.category.CategoryNodeRemover;
import com.catalog.adapter.persistence.category.CategoryNodeRetriever;
import com.catalog.adapter.persistence.category.impl.CategoryNodeInserterImpl;
import com.catalog.adapter.persistence.category.impl.CategoryNodeRemoverImpl;
import com.catalog.adapter.persistence.category.impl.CategoryNodeRetrieverImpl;
import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.factory.CategoryComponentFactory;
import com.catalog.adapter.persistence.mapper.*;
import com.catalog.domain.port.outbound.ProductPublicationRepository;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.adapter.persistence.entity.ProductEntity;
import com.catalog.adapter.persistence.mapper.impl.CategoryJpaAssemblerImpl;
import com.catalog.adapter.persistence.mapper.impl.ProductPublicationJpaAssemblerImpl;
import com.catalog.adapter.persistence.mapper.impl.VariantTypeJpaAssemblerImpl;
import com.catalog.adapter.persistence.outbox.CatalogOutboxEvent;
import com.catalog.adapter.persistence.outbox.CatalogOutboxEventProcessor;
import com.catalog.adapter.persistence.outbox.CatalogOutboxEventProducer;
import com.catalog.adapter.persistence.repository.*;
import com.catalog.adapter.persistence.adapter.*;
import com.catalog.adapter.persistence.adapter.*;
import com.catalog.adapter.persistence.adapter.VariantOptionQueryAdapter;
import com.catalog.adapter.persistence.specification.ProductSearchSpecification;
import com.catalog.adapter.persistence.specification.ProductVariantSearchSpecification;
import com.catalog.adapter.persistence.workflow.CatalogWorkflowStepRunner;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.catalog.adapter.persistence.mapper.impl.ProductJpaAssemblerImpl;
import com.catalog.adapter.persistence.adapter.CatalogPersistenceExecutor;
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
public class CatalogPersistenceConfig {

    @Bean("catalogOutboxEventSerializer")
    public OutboxEventSerializer catalogOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("catalogOutboxEventDispatcher")
    public OutboxEventDispatcher catalogOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("catalogOutboxRelay")
    public OutboxRelay<Long> catalogOutboxRelay(
            @Value("${catalog.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${catalog.outbox.hot-queue.workers:2}") int workers,
            @Value("${catalog.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${catalog.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("catalog", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("catalogDomainEventProducer")
    public DomainEventProducer catalogDomainEventProducer(
            @Qualifier("catalogOutboxStore") OutboxStore<CatalogOutboxEvent, Long> outboxStore,
            @Qualifier("catalogOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("catalogOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CatalogOutboxEventProducer(outboxStore, serializer, relay);
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
            @Value("${catalog.outbox.retention-ms:604800000}") long retentionMs,
            @Qualifier("catalogOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new CatalogOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                batchSize,
                Duration.ofMillis(retryDelayMs),
                Duration.ofMillis(claimTimeoutMs),
                Duration.ofMillis(retentionMs),
                relay
        );
    }

    @Bean
    public CatalogWorkflowStepRunner catalogWorkflowSignalEmitter(
            @Qualifier("catalogDomainEventProducer") DomainEventProducer producer,
            @Qualifier("catalogTransactionManager") PlatformTransactionManager transactionManager
    ) {
        return new CatalogWorkflowStepRunner(producer, transactionManager);
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
        return new ProductRepositoryAdapter(
                jpaAssembler, productJpaRepo, domainEventProducer, executor
        );
    }

    @Bean
    public ProductPublicationJpaAssembler productPublicationJpaAssembler(
            ProductPublicationEntityMapper productPublicationEntityMapper,
            ProductPublicationMapper productPublicationMapper
    ) {
        return new ProductPublicationJpaAssemblerImpl(
                productPublicationEntityMapper,
                productPublicationMapper
        );
    }

    @Bean
    public ProductPublicationRepository productPublicationRepository(
            ProductPublicationJpaRepository productPublicationJpaRepository,
            ProductVariantJpaRepo productVariantJpaRepo,
            ProductJpaRepo productJpaRepo,
            ProductPublicationJpaAssembler mapper,
            @Qualifier("catalogDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new ProductPublicationRepositoryAdapter(
                productPublicationJpaRepository,
                productVariantJpaRepo,
                productJpaRepo,
                mapper,
                domainEventProducer,
                executor
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
        return new VariantTypeRepositoryAdapter(
                variantTypeJpaAssembler,
                variantTypeJpaRepo,
                domainEventProducer,
                executor
        );
    }

    @Bean
    public ProductSearchSpecification productSearchSpecification(JpaContext context) {
        return new ProductSearchSpecification(
                context.getEntityManagerByManagedType(ProductEntity.class)
        );
    }

    @Bean
    public ProductVariantSearchSpecification productVariantSearchSpecification(JpaContext context) {
        return new ProductVariantSearchSpecification(
                context.getEntityManagerByManagedType(ProductEntity.class)
        );
    }

    @Bean
    public ProductQueryPort productQueryRepository(
            ProductSearchSpecification productSearchSpecification,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new ProductQueryAdapter(productSearchSpecification, executor);
    }

    @Bean
    public ProductVariantQueryPort productVariantQueryRepository(
            ProductVariantSearchSpecification productVariantSearchSpecification,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new ProductVariantQueryAdapter(productVariantSearchSpecification, executor);
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
        return new CategoryRepositoryAdapter(
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
        return new CategoryHierarchyAdapter(categoryNodeRepository, executor, idGenerator);
    }

    @Bean
    public CategoryQueryPort categoryQueryRepository(
            CategoryJpaRepo categoryJpaRepo,
            CategoryNodeRepository categoryNodeRepository,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new CategoryQueryAdapter(categoryJpaRepo, categoryNodeRepository, executor);
    }

    @Bean
    public VariantOptionQueryPort variantOptionQueryRepository(
            VariantOptionJpaRepo variantOptionQueryJpaRepo,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new VariantOptionQueryAdapter(variantOptionQueryJpaRepo, executor);
    }

    @Bean
    public VariantTypeQueryPort variantTypeQueryRepository(
            VariantTypeJpaRepo variantTypeJpaRepo,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new VariantTypeQueryAdapter(variantTypeJpaRepo, executor);
    }

    @Bean
    public MerchantAvailabilityPort merchantAvailabilityPort(
            CatalogMerchantAvailabilityJpaRepository availabilityJpaRepository
    ) {
        return new MerchantAvailabilityAdapter(availabilityJpaRepository);
    }

    @Bean
    public ProductAuditPort productAuditPort(CatalogOutboxEventJpaRepo catalogOutboxEventJpaRepo) {
        return new ProductAuditAdapter(catalogOutboxEventJpaRepo);
    }

    @Bean
    public BuyabilityQueryPort buyabilityQueryPort(
            ProductVariantJpaRepo productVariantJpaRepo,
            ProductPublicationRepository productPublicationRepository,
            ProductPublicationJpaRepository productPublicationJpaRepository,
            IdGenerator idGenerator
    ) {
        return new BuyabilityQueryAdapter(
                productVariantJpaRepo,
                productPublicationRepository,
                productPublicationJpaRepository,
                idGenerator
        );
    }
}
