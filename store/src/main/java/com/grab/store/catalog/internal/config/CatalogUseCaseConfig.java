package com.grab.store.catalog.internal.config;

import com.catalog.application.port.inbound.*;
import com.catalog.application.port.outbound.*;
import com.catalog.application.query.ProductMediaQueryMapper;
import com.catalog.application.service.*;
import com.catalog.domain.port.outbound.*;
import com.catalog.domain.service.*;
import com.catalog.domain.service.impl.*;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.storage.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;

@Configuration
public class CatalogUseCaseConfig {
    @Bean
    public SkuGenerator skuGenerator() {
        return new DefaultSkuGenerator();
    }

    @Bean
    public MatrixCombinationService variantCombination() {
        return new DefaultMatrixCombinationService();
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
    public MatrixKeyGenerator variationKeyGenerator(Comparator<ProductVariation> getVariationComparator) {
        return new DefaultMatrixKeyGenerator(getVariationComparator);
    }

    @Bean
    public VariationMatrixMatcher variationMatrixMatcher(MatrixKeyGenerator matrixKeyGenerator) {
        return new DefaultVariationMatrixMatcher(matrixKeyGenerator);
    }

    @Bean
    public MatrixCombinationSynchronizer variantCombinationManager(
            MatrixKeyGenerator matrixKeyGenerator,
            VariationMatrixMatcher variationMatrixMatcher) {
        return new DefaultMatrixCombinationSynchronizer(matrixKeyGenerator, variationMatrixMatcher);
    }

    @Bean
    public ProductMediaService productMediaService() {
        return new DefaultProductMediaService();
    }

    @Bean
    public UniqueSlugResolver uniqueSlugResolver(ProductRepository productRepository) {
        return new UniqueSlugResolver(productRepository);
    }

    @Bean
    public MediaUploadValidator mediaUploadValidator(
            @Value("${storage.upload.max-size-bytes:10485760}") long maxSizeBytes) {
        return new MediaUploadValidator(maxSizeBytes);
    }

    @Bean
    public ProductMediaQueryMapper productMediaQueryMapper(FileStoragePort fileStoragePort) {
        return new ProductMediaQueryMapper(fileStoragePort);
    }

    @Bean
    public ApplyProductStatusUseCase applyProductStatusUseCase(ProductRepository productRepository) {
        return new ApplyProductStatusService(productRepository);
    }
    @Bean
    public CheckProductPublishableUseCase checkProductPublishableUseCase(ProductRepository productRepository, IdGenerator idGenerator) {
        return new CheckProductPublishableService(productRepository, idGenerator);
    }
    @Bean
    public CreateProductMediaUploadUseCase createProductMediaUploadUseCase(ProductRepository productRepository, FileStoragePort fileStoragePort, IdGenerator idGenerator, MediaUploadValidator mediaUploadValidator) {
        return new CreateProductMediaUploadService(productRepository, fileStoragePort, idGenerator, mediaUploadValidator);
    }
    @Bean
    public CreateProductSetUseCase createProductSetUseCase(ProductRepository productRepository, CategoryRepository categoryRepository, UniqueSlugResolver uniqueSlugResolver, IdGenerator idGenerator, SkuGenerator skuGenerator, MatrixCombinationService matrixCombinationService, MatrixKeyGenerator matrixKeyGenerator) {
        return new CreateProductSetService(productRepository, categoryRepository, uniqueSlugResolver, idGenerator, skuGenerator, matrixCombinationService, matrixKeyGenerator);
    }
    @Bean
    public CreateStagedMediaUploadUseCase createStagedMediaUploadUseCase(FileStoragePort fileStoragePort, IdGenerator idGenerator, MediaUploadValidator mediaUploadValidator) {
        return new CreateStagedMediaUploadService(fileStoragePort, idGenerator, mediaUploadValidator);
    }
    @Bean
    public DeleteCategoryUseCase deleteCategoryUseCase(CategoryRepository categoryRepository, CategoryHierarchyPort categoryHierarchyPort, ProductRepository productRepository) {
        return new DeleteCategoryService(categoryRepository, categoryHierarchyPort, productRepository);
    }
    @Bean
    public DeleteProductUseCase deleteProductUseCase(ProductRepository productRepository) {
        return new DeleteProductService(productRepository);
    }
    @Bean
    public DeleteVariantUseCase deleteVariantUseCase(ProductRepository productRepository) {
        return new DeleteVariantService(productRepository);
    }
    @Bean
    public GetCategoryChildrenUseCase getCategoryChildrenUseCase(CategoryQueryPort categoryQueryRepository) {
        return new GetCategoryChildrenService(categoryQueryRepository);
    }
    @Bean
    public GetCategoryLeafNodesByNameUseCase getCategoryLeafNodesByNameUseCase(CategoryQueryPort categoryQueryRepository) {
        return new GetCategoryLeafNodesByNameService(categoryQueryRepository);
    }
    @Bean
    public GetCategoryParentUseCase getCategoryParentUseCase(CategoryQueryPort categoryQueryRepository) {
        return new GetCategoryParentService(categoryQueryRepository);
    }
    @Bean
    public GetCategoryUseCase getCategoryUseCase(CategoryRepository categoryRepository, IdGenerator idGenerator) {
        return new GetCategoryService(categoryRepository, idGenerator);
    }
    @Bean
    public GetCategoryTreeUseCase getCategoryTreeUseCase(CategoryQueryPort categoryQueryRepository) {
        return new GetCategoryTreeService(categoryQueryRepository);
    }
    @Bean
    public GetProductAuditUseCase getProductAuditUseCase(ProductAuditPort productAuditPort, ProductRepository productRepository, IdGenerator idGenerator) {
        return new GetProductAuditService(productAuditPort, productRepository, idGenerator);
    }
    @Bean
    public GetProductBySlugUseCase getProductBySlugUseCase(ProductRepository productRepository, VariantOptionQueryPort variantOptionQueryRepository, MerchantAvailabilityPort merchantAvailabilityPort, IdGenerator idGenerator, ProductMediaQueryMapper productMediaQueryMapper) {
        return new GetProductBySlugService(productRepository, variantOptionQueryRepository, merchantAvailabilityPort, idGenerator, productMediaQueryMapper);
    }
    @Bean
    public GetProductUseCase getProductUseCase(ProductRepository productRepository, ProductQueryPort productQueryRepository, VariantOptionQueryPort variantOptionQueryRepository, IdGenerator idGenerator, CategoryQueryPort categoryQueryRepository, MatrixKeyGenerator matrixKeyGenerator, ProductMediaQueryMapper productMediaQueryMapper) {
        return new GetProductService(productRepository, productQueryRepository, variantOptionQueryRepository, idGenerator, categoryQueryRepository, matrixKeyGenerator, productMediaQueryMapper);
    }
    @Bean
    public GetVariantOptionsByNameUseCase getVariantOptionsByNameUseCase(VariantOptionQueryPort variantOptionQueryRepository) {
        return new GetVariantOptionsByNameService(variantOptionQueryRepository);
    }
    @Bean
    public GetVariantUseCase getVariantUseCase(ProductRepository productRepository, VariantOptionQueryPort variantOptionQueryRepository, IdGenerator idGenerator, MatrixKeyGenerator matrixKeyGenerator) {
        return new GetVariantService(productRepository, variantOptionQueryRepository, idGenerator, matrixKeyGenerator);
    }
    @Bean
    public GetVariantTypesByNameUseCase getVariantTypesByNameUseCase(VariantTypeQueryPort variantTypeQueryRepository) {
        return new GetVariantTypesByNameService(variantTypeQueryRepository);
    }
    @Bean
    public ListProductPublicationsUseCase listProductPublicationsUseCase(ProductRepository productRepository, ProductQueryPort productQueryRepository, IdGenerator idGenerator) {
        return new ListProductPublicationsService(productRepository, productQueryRepository, idGenerator);
    }
    @Bean
    public ModerateProductUseCase moderateProductUseCase(ProductRepository productRepository, CategoryRepository categoryRepository) {
        return new ModerateProductService(productRepository, categoryRepository);
    }
    @Bean
    public ProductSearchUseCase productSearchUseCase(ProductQueryPort productQueryRepository, CategoryQueryPort categoryRepository, FileStoragePort fileStoragePort) {
        return new ProductSearchService(productQueryRepository, categoryRepository, fileStoragePort);
    }
    @Bean
    public ProductVariantSearchUseCase productVariantSearchUseCase(ProductVariantQueryPort productVariantQueryRepository, CategoryQueryPort categoryRepository) {
        return new ProductVariantSearchService(productVariantQueryRepository, categoryRepository);
    }
    @Bean
    public PublishProductToChannelUseCase publishProductToChannelUseCase(ProductRepository productRepository, ProductPublicationRepository productPublicationRepository) {
        return new PublishProductToChannelService(productRepository, productPublicationRepository);
    }
    @Bean
    public ReplaceProductDescriptionsUseCase replaceProductDescriptionsUseCase(ProductRepository productRepository, IdGenerator idGenerator) {
        return new ReplaceProductDescriptionsService(productRepository, idGenerator);
    }
    @Bean
    public ReplaceProductMediaUseCase replaceProductMediaUseCase(ProductRepository productRepository, FileStoragePort fileStoragePort, IdGenerator idGenerator, ProductMediaService productMediaService) {
        return new ReplaceProductMediaService(productRepository, fileStoragePort, idGenerator, productMediaService);
    }
    @Bean
    public RestoreVariantUseCase restoreVariantUseCase(ProductRepository productRepository) {
        return new RestoreVariantService(productRepository);
    }
    @Bean
    public SaveCategoryUseCase saveCategoryUseCase(CategoryRepository categoryRepository, IdGenerator idGenerator) {
        return new SaveCategoryService(categoryRepository, idGenerator);
    }
    @Bean
    public SetVariantMediaUseCase setVariantMediaUseCase(ProductRepository productRepository, ProductMediaService productMediaService) {
        return new SetVariantMediaService(productRepository, productMediaService);
    }
    @Bean
    public UnpublishProductFromChannelUseCase unpublishProductFromChannelUseCase(ProductRepository productRepository, ProductPublicationRepository productPublicationRepository) {
        return new UnpublishProductFromChannelService(productRepository, productPublicationRepository);
    }
    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductRepository productRepository, CategoryRepository categoryRepository, UniqueSlugResolver uniqueSlugResolver, IdGenerator idGenerator, SkuGenerator skuGenerator, MatrixCombinationService matrixCombinationService, MatrixCombinationSynchronizer matrixCombinationSynchronizer, MatrixKeyGenerator matrixKeyGenerator) {
        return new UpdateProductService(productRepository, categoryRepository, uniqueSlugResolver, idGenerator, skuGenerator, matrixCombinationService, matrixCombinationSynchronizer, matrixKeyGenerator);
    }
    @Bean
    public UpdateProductStatusUseCase updateProductStatusUseCase(ProductRepository productRepository, CategoryRepository categoryRepository) {
        return new UpdateProductStatusService(productRepository, categoryRepository);
    }
    @Bean
    public UpdateVariantUseCase updateVariantUseCase(ProductRepository productRepository) {
        return new UpdateVariantService(productRepository);
    }
    @Bean
    public UpsertMerchantAvailabilityUseCase upsertMerchantAvailabilityUseCase(MerchantAvailabilityPort merchantAvailabilityPort) {
        return new UpsertMerchantAvailabilityService(merchantAvailabilityPort);
    }
    @Bean
    public VariationMatrixUseCase variationMatrixUseCase(MatrixCombinationService matrixCombinationService, MatrixKeyGenerator matrixKeyGenerator, IdGenerator idGenerator, VariationMatrixMatcher matcher) {
        return new VariationMatrixService(matrixCombinationService, matrixKeyGenerator, idGenerator, matcher);
    }
}
