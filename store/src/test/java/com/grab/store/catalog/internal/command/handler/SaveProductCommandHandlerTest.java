package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.VariantOption;
import com.catalog.domain.aggregate.VariantType;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.service.VariantCombinationService;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.SaveProductCommand;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.StandaloneVariantDefaults;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveProductCommandHandlerTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UniqueSlugResolver uniqueSlugResolver;
    @Mock
    private SkuGenerator skuGenerator;
    @Mock
    private IdGenerator idGenerator;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private SaveProductCommandHandler handler;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String VARIANT_ID = "variant-789";
    private static final String COLOR_TYPE_ID = "color-type-001";
    private static final String RED_OPTION_ID = "red-option-001";

    @BeforeEach
    void setUp() {
        handler = new SaveProductCommandHandler(
                productRepository,
                categoryRepository,
                uniqueSlugResolver,
                skuGenerator,
                idGenerator);
    }

    @Test
    void handle_withVariantsAddsAllVariantsToProduct() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Id colorTypeId = new CommonId(COLOR_TYPE_ID);
        Id redOptionId = new CommonId(RED_OPTION_ID);

        SaveProductCommand.Variation variation = new SaveProductCommand.Variation(
                "Red", redOptionId, colorTypeId, "Color"
        );
        SaveProductCommand.Variant variant = new SaveProductCommand.Variant(
                variantId, "SKU-RED-001", "ACTIVE", List.of(variation)
        );
        SaveProductCommand command = new SaveProductCommand(
                new SaveProductCommand.Product(
                        productId,
                        "Product with Variants",
                        categoryId,
                        null,
                        null,
                        null,
                        false,
                        null,
                        false,
                        List.of(),
                        List.of(),
                        List.of(variant))
        );

        when(productRepository.find(productId)).thenReturn(Optional.empty());
        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(Category.createRoot(categoryId, "Category")));
        when(uniqueSlugResolver.resolve(null, "Product with Variants", null)).thenReturn("product-with-variants");

        handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getVariants()).hasSize(1);
        assertThat(savedProduct.getSlug()).isEqualTo("product-with-variants");
        assertThat(savedProduct.getVariants().getFirst().getSku()).isEqualTo("SKU-RED-001");
        assertThat(savedProduct.getVariants().getFirst().getStatus()).isEqualTo(ProductVariantStatus.ACTIVE);
    }

    @Test
    void handle_existingProductThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        when(productRepository.find(productId)).thenReturn(Optional.of(Product.create(productId, "Product", categoryId)));

        SaveProductCommand command = new SaveProductCommand(
                new SaveProductCommand.Product(
                        productId,
                        "Product",
                        categoryId,
                        null,
                        null,
                        null,
                        false,
                        null,
                        false,
                        List.of(),
                        List.of(),
                        List.of()
                )
        );

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.product.product_already_existed"));
    }

    @Test
    void handle_categoryNotFoundThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);

        SaveProductCommand command = new SaveProductCommand(
                new SaveProductCommand.Product(
                        productId,
                        "Product",
                        categoryId,
                        null,
                        null,
                        null,
                        false,
                        null,
                        false,
                        List.of(),
                        List.of(),
                        List.of()
                )
        );

        when(productRepository.find(productId)).thenReturn(Optional.empty());
        when(categoryRepository.find(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.not_found"));
    }

    @Test
    void handle_duplicateSkuThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Id colorTypeId = new CommonId(COLOR_TYPE_ID);
        Id redOptionId = new CommonId(RED_OPTION_ID);

        SaveProductCommand command = new SaveProductCommand(
                new SaveProductCommand.Product(
                        productId,
                        "Product with Variants",
                        categoryId,
                        null,
                        null,
                        null,
                        false,
                        null,
                        false,
                        List.of(),
                        List.of(),
                        List.of(new SaveProductCommand.Variant(
                                variantId,
                                "SKU-RED-001",
                                "ACTIVE",
                                List.of(new SaveProductCommand.Variation("Red", redOptionId, colorTypeId, "Color"))
                        )))
        );

        when(productRepository.find(productId)).thenReturn(Optional.empty());
        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(Category.createRoot(categoryId, "Category")));
        when(uniqueSlugResolver.resolve(null, "Product with Variants", null)).thenReturn("product-with-variants");
        when(productRepository.isSkuTaken("SKU-RED-001", null)).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.variant.sku_already_exists"));
    }

    @Test
    void handle_noVariantsAndNoUsableVariantTypes_createsDefaultVariant() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id generatedVariantId = new CommonId("generated-variant-1");

        SaveProductCommand command = new SaveProductCommand(
                new SaveProductCommand.Product(
                        productId,
                        "Simple Product",
                        categoryId,
                        null,
                        null,
                        null,
                        false,
                        null,
                        false,
                        List.of(),
                        List.of(),
                        List.of()
                ));

        when(productRepository.find(productId)).thenReturn(Optional.empty());
        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(Category.createRoot(categoryId, "Category")));
        when(uniqueSlugResolver.resolve(null, "Simple Product", null)).thenReturn("simple-product");
        when(idGenerator.generateId()).thenReturn(generatedVariantId);
        when(skuGenerator.generate(any())).thenReturn("SMP");

        handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getVariants()).hasSize(1);
        assertThat(savedProduct.getVariants().getFirst().getId()).isEqualTo(generatedVariantId);
        assertThat(savedProduct.getVariants().getFirst().getSku()).isEqualTo("SMP");
        assertThat(savedProduct.getVariants().getFirst().getVariations()).hasSize(1);
        assertThat(savedProduct.getVariants().getFirst().getVariations().iterator().next().getTypeName())
                .isEqualTo(StandaloneVariantDefaults.TYPE_NAME);
        assertThat(savedProduct.getVariants().getFirst().getVariations().iterator().next().getOptionName())
                .isEqualTo(StandaloneVariantDefaults.OPTION_NAME);
        assertThat(savedProduct.getVariants().getFirst().getVariations().iterator().next().getTypeId().getValue())
                .isEqualTo(StandaloneVariantDefaults.TYPE_ID);
        assertThat(savedProduct.getVariants().getFirst().getVariations().iterator().next().getOptionId().getValue())
                .isEqualTo(StandaloneVariantDefaults.OPTION_ID);
        assertThat(savedProduct.getVariants().getFirst().getStatus()).isEqualTo(ProductVariantStatus.ACTIVE);
    }

    @Test
    void handle_duplicateCombinationInRequest_throwsVariantAddFailed() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id colorTypeId = new CommonId(COLOR_TYPE_ID);
        Id redOptionId = new CommonId(RED_OPTION_ID);

        SaveProductCommand.Variation duplicateVariation =
                new SaveProductCommand.Variation("Red", redOptionId, colorTypeId, "Color");

        SaveProductCommand command = new SaveProductCommand(
                new SaveProductCommand.Product(
                        productId,
                        "Product with Variants",
                        categoryId,
                        null,
                        null,
                        null,
                        false,
                        null,
                        false,
                        List.of(),
                        List.of(),
                        List.of(
                                new SaveProductCommand.Variant(new CommonId("variant-1"), "SKU-RED-001", "ACTIVE", List.of(duplicateVariation)),
                                new SaveProductCommand.Variant(new CommonId("variant-2"), "SKU-RED-002", "ACTIVE", List.of(duplicateVariation))
                        )
                ) );

        when(productRepository.find(productId)).thenReturn(Optional.empty());
        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(Category.createRoot(categoryId, "Category")));
        when(uniqueSlugResolver.resolve(null, "Product with Variants", null)).thenReturn("product-with-variants");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.variant.add_failed"));
    }
}
