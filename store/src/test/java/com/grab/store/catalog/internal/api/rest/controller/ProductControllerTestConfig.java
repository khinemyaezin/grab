package com.grab.store.catalog.internal.api.rest.controller;

import com.catalog.domain.service.impl.*;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.assembler.*;
import com.grab.store.catalog.internal.api.rest.mapper.*;
import com.grab.store.catalog.internal.api.rest.service.ProductCommandService;
import com.grab.store.catalog.internal.api.rest.service.ProductFacadeService;
import com.grab.store.catalog.internal.api.rest.service.ProductQueryService;
import com.grab.store.catalog.internal.api.rest.service.VariantCommandService;
import com.grab.store.catalog.internal.command.handler.InMemoryProductRepositoryTest;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.cqrs.command.impl.DefaultCommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.cqrs.query.impl.DefaultQueryBus;
import com.grab.store.catalog.internal.query.handler.ProductCombinationQueryHandler;
import com.grab.store.catalog.internal.util.ProductSKUGenerator;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.store.catalog.internal.command.handler.SyncVariantsCommandHandler;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.service.VariantCombinationService;
import com.catalog.domain.service.VariantDeletionStrategy;
import com.catalog.domain.service.VariationCombinationManager;
import com.catalog.domain.service.VariationKeyGenerator;
import com.catalog.domain.repository.ProductRepository;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Comparator;
import java.util.List;

@TestConfiguration
public class ProductControllerTestConfig {
    @Bean
    public IdGenerator idGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public SkuGenerator skuGenerator() {
        return new ProductSKUGenerator();
    }

    @Bean
    public VariantCombinationService variantCombinationService() {
        return new DefaultVariantCombinationService();
    }

    @Bean
    public VariantDeletionStrategy variantDeletionStrategy() {
        return new FullOptionHardDeleteStrategy();
    }

    @Bean
    public Comparator<ProductVariation> getVariationComparator() {
        return new ProductVariationComparator();
    }

    @Bean
    public VariationKeyGenerator variationKeyGenerator(Comparator<ProductVariation> getVariationComparator) {
        return new DefaultVariationKeyGenerator(getVariationComparator);
    }

    @Bean
    public VariationCombinationManager variationCombinationManager(VariationKeyGenerator variationKeyGenerator) {
        return new DefaultVariationCombinationManager(variationKeyGenerator);
    }

    @Bean
    public InMemoryProductRepositoryTest productRepository() {
        return new InMemoryProductRepositoryTest();
    }

    @Bean
    public SyncVariantsCommandHandler syncVariantsCommandHandler(
            ProductRepository productRepository,
            VariantCombinationService variantCombinationService,
            VariationCombinationManager variationCombinationManager,
            VariationKeyGenerator variationKeyGenerator,
            VariantDeletionStrategy variantDeletionStrategy
    ) {
        return new SyncVariantsCommandHandler(
                productRepository,
                variantCombinationService,
                variationCombinationManager,
                variationKeyGenerator,
                variantDeletionStrategy
        );
    }

    @Bean
    public ProductCombinationQueryHandler productCombinationQueryHandler(
            IdGenerator idGenerator,
            VariationCombinationManager variationCombinationManager,
            VariantCombinationService variantCombinationService,
            VariantDeletionStrategy variantDeletionStrategy,
            SkuGenerator skuGenerator) {
        return new ProductCombinationQueryHandler(
                idGenerator,
                variationCombinationManager,
                variantCombinationService,
                variantDeletionStrategy,
                skuGenerator);
    }

    // ========== CQRS Infrastructure ==========

    @Bean
    public QueryBus queryBus(List<QueryHandler<?, ?>> queryHandlers) {
        return new DefaultQueryBus(queryHandlers);
    }

    @Bean
    public CommandBus commandBus(List<CommandHandler<?, ?>> commandHandlers) {
        return new DefaultCommandBus(commandHandlers);
    }

    @Bean
    public IdMapper idMapper(IdGenerator idGenerator) {
        return new IdMapper(idGenerator);
    }

    @Bean
    public ProductCombinationDtoMapper productCombinationDtoMapper() {
        return Mappers.getMapper(ProductCombinationDtoMapper.class);
    }

    @Bean
    public SaveProductDtoMapper saveProductDtoMapper() {
        return Mappers.getMapper(SaveProductDtoMapper.class);
    }

    @Bean
    public ProductCombinationModelAssembler buildProductModelAssembler() {
        return new ProductCombinationModelAssembler();
    }

    @Bean
    public ProductSummaryModelAssembler buildProductSummaryModelAssembler() {
        return new ProductSummaryModelAssembler();
    }

    @Bean
    public ProductSummaryQueryMapper productSummaryQueryMapper() {
        return Mappers.getMapper(ProductSummaryQueryMapper.class);
    }

    @Bean
    public ProductSummaryDtoMapper productSummaryDtoMapper() {
        return Mappers.getMapper(ProductSummaryDtoMapper.class);
    }

    @Bean
    public DeleteProductModelAssembler deleteProductModelAssembler() {
        return new DeleteProductModelAssembler();
    }

    @Bean
    public GetProductDtoMapper getProductDtoMapper() {
        return Mappers.getMapper(GetProductDtoMapper.class);
    }

    @Bean
    public GetProductBySlugDtoMapper getProductBySlugDtoMapper() {
        return Mappers.getMapper(GetProductBySlugDtoMapper.class);
    }

    @Bean
    public GetProductModelAssembler getProductModelAssembler() {
        return new GetProductModelAssembler();
    }

    @Bean
    public GetProductBySlugModelAssembler getProductBySlugModelAssembler() {
        return new GetProductBySlugModelAssembler();
    }

    @Bean
    public UpdateProductDtoMapper getUpdateProductDtoMapper(){
        return Mappers.getMapper(UpdateProductDtoMapper.class);
    }

    @Bean
    public UpdateVariantDtoMapper getUpdateVariantDtoMapper(){
        return Mappers.getMapper(UpdateVariantDtoMapper.class);
    }

    @Bean
    public UpdateProductModelAssembler updateProductModelAssembler() {
        return new UpdateProductModelAssembler();
    }

    @Bean
    public UpdateVariantModelAssembler updateVariantModelAssembler() {
        return new UpdateVariantModelAssembler();
    }

    @Bean
    public UpdateProductStatusDtoMapper getUpdateProductStatusDtoMapper(){
        return Mappers.getMapper(UpdateProductStatusDtoMapper.class);
    }

    @Bean
    public DeleteVariantDtoMapper getDeleteVariantDtoMapper(){
        return Mappers.getMapper(DeleteVariantDtoMapper.class);
    }

    @Bean
    public UpdateProductStatusModelAssembler updateProductStatusModelAssembler() {
        return new UpdateProductStatusModelAssembler();
    }

    @Bean
    public DeleteVariantModelAssembler deleteVariantModelAssembler() {
        return new DeleteVariantModelAssembler();
    }

    @Bean
    public RestoreVariantDtoMapper getRestoreVariantDtoMapper(){
        return Mappers.getMapper(RestoreVariantDtoMapper.class);
    }

    @Bean
    public RestoreVariantModelAssembler restoreVariantModelAssembler() {
        return new RestoreVariantModelAssembler();
    }

    @Bean
    public SyncVariantsDtoMapper  syncVariantsDtoMapper() {
        return Mappers.getMapper(SyncVariantsDtoMapper.class);
    }

    @Bean
    public SyncVariantsModelAssembler syncVariantsModelAssembler() {
        return new SyncVariantsModelAssembler();
    }

    @Bean
    public ProductQueryService productQueryService(
            QueryBus queryBus,
            GetProductDtoMapper getProductDtoMapper,
            GetProductBySlugDtoMapper getProductBySlugDtoMapper,
            GetProductModelAssembler getProductModelAssembler,
            GetProductBySlugModelAssembler getProductBySlugModelAssembler,
            ProductCombinationDtoMapper productCombinationDtoMapper,
            ProductCombinationModelAssembler productCombinationModelAssembler,
            ProductSummaryModelAssembler productSummaryModelAssembler,
            ProductSummaryQueryMapper productSummaryQueryMapper,
            ProductSummaryDtoMapper productSummaryDtoMapper
    ) {
        return new ProductQueryService(
                queryBus,
                getProductDtoMapper,
                getProductBySlugDtoMapper,
                getProductModelAssembler,
                getProductBySlugModelAssembler,
                productCombinationDtoMapper,
                productCombinationModelAssembler,
                productSummaryModelAssembler,
                productSummaryQueryMapper,
                productSummaryDtoMapper
        );
    }

    @Bean
    public ProductCommandService productCommandService(
            CommandBus commandBus,
            SaveProductDtoMapper saveProductDtoMapper,
            DeleteProductModelAssembler deleteProductModelAssembler,
            UpdateProductDtoMapper updateProductDtoMapper,
            UpdateProductStatusDtoMapper updateProductStatusDtoMapper,
            UpdateProductModelAssembler updateProductModelAssembler,
            UpdateProductStatusModelAssembler updateProductStatusModelAssembler,
            IdGenerator idGenerator
    ) {
        return new ProductCommandService(
                commandBus,
                saveProductDtoMapper,
                deleteProductModelAssembler,
                updateProductDtoMapper,
                updateProductStatusDtoMapper,
                updateProductModelAssembler,
                updateProductStatusModelAssembler,
                idGenerator
        );
    }

    @Bean
    public VariantCommandService variantCommandService(
            CommandBus commandBus,
            UpdateVariantDtoMapper updateVariantDtoMapper,
            DeleteVariantDtoMapper deleteVariantDtoMapper,
            RestoreVariantDtoMapper restoreVariantDtoMapper,
            SyncVariantsDtoMapper syncVariantsDtoMapper,
            UpdateVariantModelAssembler updateVariantModelAssembler,
            DeleteVariantModelAssembler deleteVariantModelAssembler,
            RestoreVariantModelAssembler restoreVariantModelAssembler,
            SyncVariantsModelAssembler syncVariantsModelAssembler
    ) {
        return new VariantCommandService(
                commandBus,
                updateVariantDtoMapper,
                deleteVariantDtoMapper,
                restoreVariantDtoMapper,
                syncVariantsDtoMapper,
                updateVariantModelAssembler,
                deleteVariantModelAssembler,
                restoreVariantModelAssembler,
                syncVariantsModelAssembler
        );
    }

    @Bean
    public ProductFacadeService productFacadeService(
            ProductQueryService productQueryService,
            ProductCommandService productCommandService,
            VariantCommandService variantCommandService
    ) {
        return new ProductFacadeService(productQueryService, productCommandService, variantCommandService);
    }
}
