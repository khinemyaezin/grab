package com.catalog.application.command.handler;

import com.catalog.application.service.UpdateProductService;

import com.catalog.domain.service.MatrixCombinationService;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.port.outbound.CategoryRepository;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.service.MatrixCombinationSynchronizer;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.catalog.application.model.write.UpdateProductCommand;
import com.catalog.application.model.write.UpdateProductResult;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.service.StandaloneVariationFactory;
import com.catalog.application.service.UniqueSlugResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UniqueSlugResolver uniqueSlugResolver;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private SkuGenerator skuGenerator;
    @Mock
    private MatrixCombinationService matrixCombinationService;
    @Mock
    private MatrixCombinationSynchronizer matrixCombinationSynchronizer;
    @Mock
    private MatrixKeyGenerator matrixKeyGenerator;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private UpdateProductService service;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String NEW_CATEGORY_ID = "category-789";
    private static final String VARIANT_ID = "variant-abc";
    private static final String STANDALONE_SKU = "SKU-STANDALONE";
    private static final String UPDATED_SKU = "SKU-UPDATED";

    @BeforeEach
    void setUp() {
        service = new UpdateProductService(
                productRepository,
                categoryRepository,
                uniqueSlugResolver,
                idGenerator,
                skuGenerator,
                matrixCombinationService,
                matrixCombinationSynchronizer,
                matrixKeyGenerator
                );
    }

    @Test
    void handle_updatesNameAndCategory() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id newCategoryId = new CommonId(NEW_CATEGORY_ID);

        Product existing = Product.create(productId, productId, "Old Name", categoryId);
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(existing));
        when(categoryRepository.find(newCategoryId)).thenReturn(Optional.of(Category.createRoot(newCategoryId, "Category")));
        when(uniqueSlugResolver.resolve(productId, null, "New Name", PRODUCT_ID)).thenReturn("new-name");

        UpdateProductCommand command = new UpdateProductCommand(
                productId,
                productId,
                "New Name",
                newCategoryId,
                null,
                null,
                null
        );
        UpdateProductResult result = service.execute(command);

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();

        assertThat(saved.getName()).isEqualTo("New Name");
        assertThat(saved.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(saved.getSlug()).isEqualTo("new-name");

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.categoryId()).isEqualTo(NEW_CATEGORY_ID);
        assertThat(result.status()).isEqualTo(ProductStatus.DRAFT.name());
        assertThat(result.slug()).isEqualTo("new-name");
    }

    @Test
    void handle_whenStatusActive_leavesDraftUntilApplyStatus() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Product existing = publishableProduct(productId, categoryId, variantId);
        stubProductAndCategory(productId, categoryId, existing);

        UpdateProductCommand command = new UpdateProductCommand(
                productId,
                productId,
                "Old Name",
                categoryId,
                null,
                null,
                null,
                "ACTIVE"
        );
        UpdateProductResult result = service.execute(command);

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(result.status()).isEqualTo(ProductStatus.DRAFT.name());
    }

    @Test
    void handle_whenStatusOmitted_leavesDraft() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Product existing = publishableProduct(productId, categoryId, variantId);
        stubProductAndCategory(productId, categoryId, existing);

        UpdateProductCommand command = new UpdateProductCommand(
                productId,
                productId,
                "Old Name",
                categoryId,
                null,
                null,
                null
        );
        UpdateProductResult result = service.execute(command);

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(result.status()).isEqualTo(ProductStatus.DRAFT.name());
    }

    @Test
    void handle_productNotFoundThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        when(productRepository.find(productId, productId)).thenReturn(Optional.empty());

        UpdateProductCommand command = new UpdateProductCommand(
                productId,
                productId,
                "Name",
                new CommonId(CATEGORY_ID),
                null,
                null,
                null
        );

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.product.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    @Test
    void handle_categoryNotFoundThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id missingCategoryId = new CommonId(NEW_CATEGORY_ID);
        Product existing = Product.create(productId, productId, "Old Name", categoryId);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(existing));
        when(categoryRepository.find(missingCategoryId)).thenReturn(Optional.empty());

        UpdateProductCommand command = new UpdateProductCommand(
                productId,
                productId,
                "New Name",
                missingCategoryId,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.category.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    @Test
    void handle_fullSyncStandaloneSameSku_keepsVariantIdAndSkipsMatrix() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Product existing = productWithStandalone(productId, categoryId, variantId, STANDALONE_SKU);

        stubProductAndCategory(productId, categoryId, existing);

        UpdateProductResult result = service.execute(standaloneFullSyncCommand(
                productId,
                categoryId,
                STANDALONE_SKU,
                List.of(),
                List.of()
        ));

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();
        ProductVariant savedVariant = saved.getVariants().getFirst();

        assertThat(savedVariant.getId()).isEqualTo(variantId);
        assertThat(savedVariant.getSku()).isEqualTo(STANDALONE_SKU);
        assertThat(result.variants()).containsExactly(new UpdateProductResult.VariantRef(VARIANT_ID, STANDALONE_SKU));
        verifyNoInteractions(matrixCombinationService, matrixCombinationSynchronizer, matrixKeyGenerator);
    }

    @Test
    void handle_fullSyncStandaloneNewSku_updatesSkuAndKeepsVariantId() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Product existing = productWithStandalone(productId, categoryId, variantId, STANDALONE_SKU);

        stubProductAndCategory(productId, categoryId, existing);

        UpdateProductResult result = service.execute(standaloneFullSyncCommand(
                productId,
                categoryId,
                UPDATED_SKU,
                List.of(),
                List.of()
        ));

        verify(productRepository).save(productCaptor.capture());
        ProductVariant savedVariant = productCaptor.getValue().getVariants().getFirst();

        assertThat(savedVariant.getId()).isEqualTo(variantId);
        assertThat(savedVariant.getSku()).isEqualTo(UPDATED_SKU);
        assertThat(result.variants()).containsExactly(new UpdateProductResult.VariantRef(VARIANT_ID, UPDATED_SKU));
        verifyNoInteractions(matrixCombinationService, matrixCombinationSynchronizer, matrixKeyGenerator);
    }

    @Test
    void handle_fullSyncStandaloneWithSyntheticVariationIds_keepsVariantId() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Product existing = productWithStandalone(productId, categoryId, variantId, STANDALONE_SKU);

        stubProductAndCategory(productId, categoryId, existing);

        UpdateProductCommand.Variation standaloneVariation = new UpdateProductCommand.Variation(
                new CommonId(StandaloneVariationFactory.TYPE_ID),
                new CommonId(StandaloneVariationFactory.OPTION_ID)
        );
        UpdateProductCommand.VariantType standaloneType = new UpdateProductCommand.VariantType(
                new CommonId(StandaloneVariationFactory.TYPE_ID),
                List.of(new UpdateProductCommand.VariantOption(
                        new CommonId(StandaloneVariationFactory.OPTION_ID),
                        "Default Title"
                ))
        );

        service.execute(standaloneFullSyncCommand(
                productId,
                categoryId,
                STANDALONE_SKU,
                List.of(standaloneVariation),
                List.of(standaloneType)
        ));

        verify(productRepository).save(productCaptor.capture());
        ProductVariant savedVariant = productCaptor.getValue().getVariants().getFirst();

        assertThat(savedVariant.getId()).isEqualTo(variantId);
        assertThat(savedVariant.getSku()).isEqualTo(STANDALONE_SKU);
        verifyNoInteractions(matrixCombinationService, matrixCombinationSynchronizer, matrixKeyGenerator);
    }

    private void stubProductAndCategory(Id productId, Id categoryId, Product existing) {
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(existing));
        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(Category.createRoot(categoryId, "Category")));
        when(uniqueSlugResolver.resolve(productId, null, "Old Name", PRODUCT_ID)).thenReturn("old-name");
    }

    private Product productWithStandalone(Id productId, Id categoryId, Id variantId, String sku) {
        Product product = Product.create(productId, productId, "Old Name", categoryId);
        product.addVariant(ProductVariant.create(
                variantId,
                sku,
                List.of(new ProductVariation(
                        new CommonId(StandaloneVariationFactory.OPTION_ID),
                        new CommonId(StandaloneVariationFactory.TYPE_ID)
                ))
        ));
        return product;
    }

    private Product publishableProduct(Id productId, Id categoryId, Id variantId) {
        Product product = Product.create(
                productId,
                productId,
                "Old Name",
                categoryId,
                null,
                null,
                List.of(Description.create(new CommonId("desc-1"), "Default", "Title", "A description")),
                List.of(new ProductMedia(
                        new CommonId("media-1"),
                        "merchants/m/products/p/1.jpg",
                        "http://minio/1.jpg",
                        "image/jpeg",
                        0
                ))
        );
        product.addVariant(ProductVariant.create(
                variantId,
                STANDALONE_SKU,
                List.of(new ProductVariation(
                        new CommonId(StandaloneVariationFactory.OPTION_ID),
                        new CommonId(StandaloneVariationFactory.TYPE_ID)
                ))
        ));
        return product;
    }

    private UpdateProductCommand standaloneFullSyncCommand(
            Id productId,
            Id categoryId,
            String sku,
            List<UpdateProductCommand.Variation> variations,
            List<UpdateProductCommand.VariantType> variantTypes
    ) {
        return new UpdateProductCommand(
                productId,
                productId,
                "Old Name",
                categoryId,
                null,
                null,
                new UpdateProductCommand.VariantSync(
                        UpdateProductCommand.VariantSyncIntent.FULL_SYNC,
                        List.of(new UpdateProductCommand.Variant(sku, "", variations)),
                        variantTypes
                )
        );
    }
}
