package com.catalog.adapter.persistence.config;

import com.catalog.domain.port.outbound.CategoryRepository;
import com.catalog.domain.port.outbound.VariantTypeRepository;
import com.catalog.application.port.outbound.CategoryHierarchyQueryPort;
import com.catalog.domain.port.outbound.CategoryHierarchyRepository;
import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.port.outbound.MerchantAvailabilityQueryPort;
import com.catalog.domain.port.outbound.MerchantAvailabilityRepository;
import com.catalog.application.port.outbound.ProductAuditQueryPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.port.outbound.ProductVariantQueryPort;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import com.catalog.application.port.outbound.PublishedProductChannelQueryPort;
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
import com.catalog.adapter.persistence.repository.*;
import com.catalog.adapter.persistence.repository.impl.*;
import com.catalog.adapter.persistence.adapter.*;
import com.catalog.adapter.persistence.adapter.VariantOptionQueryAdapter;
import com.catalog.adapter.persistence.specification.ProductSearchSpecification;
import com.catalog.adapter.persistence.specification.ProductVariantSearchSpecification;
import com.catalog.adapter.persistence.workflow.CatalogWorkflowStepRunner;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.support.PersistenceExecutor;
import com.catalog.adapter.persistence.mapper.impl.ProductJpaAssemblerImpl;
import com.catalog.adapter.persistence.adapter.CatalogPersistenceExecutor;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import com.nestedset.app.config.JpaNestedSetRepositoryConfiguration;
import com.nestedset.app.service.TreeBuilder;
import com.nestedset.app.service.TreeBuilderImpl;
import com.catalog.adapter.persistence.adapter.CatalogSecurityManifestPublicationAdapter;
import com.catalog.adapter.persistence.adapter.CatalogSecurityManifestPublicationQueryAdapter;
import com.catalog.adapter.persistence.entity.CatalogSecurityManifestPublicationEntity;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration
public class CatalogPersistenceConfig {

    @Bean("catalogPersistenceExecutor")
    public PersistenceExecutor catalogPersistenceExecutor() {
        return new CatalogPersistenceExecutor();
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
            ProductJpaRepo productJpaRepo,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new ProductQueryAdapter(productSearchSpecification, productJpaRepo, executor);
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
    public CategoryHierarchyQueryPort categoryHierarchyQueryPort(
            CategoryNodeRepository categoryNodeRepository,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor,
            IdGenerator idGenerator
    ) {
        return new CategoryHierarchyQueryAdapter(categoryNodeRepository, executor, idGenerator);
    }

    @Bean
    public CategoryHierarchyRepository categoryHierarchyRepository(
            CategoryNodeRepository categoryNodeRepository,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new CategoryHierarchyRepositoryAdapter(categoryNodeRepository, executor);
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
    public MerchantAvailabilityQueryPort merchantAvailabilityQueryPort(
            CatalogMerchantAvailabilityJpaRepository availabilityJpaRepository
    ) {
        return new MerchantAvailabilityQueryAdapter(availabilityJpaRepository);
    }

    @Bean
    public MerchantAvailabilityRepository merchantAvailabilityRepository(
            CatalogMerchantAvailabilityJpaRepository availabilityJpaRepository
    ) {
        return new MerchantAvailabilityRepositoryAdapter(availabilityJpaRepository);
    }

    @Bean
    public ProductAuditQueryPort productAuditQueryPort(CatalogOutboxEventJpaRepo catalogOutboxEventJpaRepo) {
        return new ProductAuditQueryAdapter(catalogOutboxEventJpaRepo);
    }

    @Bean
    public BuyabilityQueryPort buyabilityQueryPort(
            ProductVariantJpaRepo productVariantJpaRepo,
            ProductPublicationJpaRepository productPublicationJpaRepository,
            IdGenerator idGenerator
    ) {
        return new BuyabilityQueryAdapter(
                productVariantJpaRepo,
                productPublicationJpaRepository,
                idGenerator
        );
    }

    @Bean
    public PublishedProductChannelQueryPort publishedProductChannelQueryPort(
            ProductPublicationJpaRepository productPublicationJpaRepository,
            @Qualifier("catalogPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new PublishedProductChannelQueryAdapter(
                productPublicationJpaRepository,
                executor
        );
    }

    @Bean("catalogSecurityManifestPublicationQueryPort")
    public SecurityManifestPublicationQueryPort catalogSecurityManifestPublicationQueryPort(JpaContext context) {
        EntityManager entityManager = context.getEntityManagerByManagedType(CatalogSecurityManifestPublicationEntity.class);
        return new CatalogSecurityManifestPublicationQueryAdapter(entityManager);
    }

    @Bean("catalogSecurityManifestPublicationPort")
    public SecurityManifestPublicationPort catalogSecurityManifestPublicationPort(
            JpaContext context,
            @Qualifier("catalogDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("catalogSecurityManifestEventFactory") SecurityManifestEventFactory eventFactory,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        return new CatalogSecurityManifestPublicationAdapter(
                context,
                outbox,
                eventFactory,
                Duration.ofMillis(intervalMs)
        );
    }
}
