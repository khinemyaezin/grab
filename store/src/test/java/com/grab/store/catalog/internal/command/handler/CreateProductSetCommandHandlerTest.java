package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.service.VariantCombinationService;
import com.catalog.domain.service.VariationCombinationManager;
import com.catalog.domain.service.VariationKeyGenerator;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.VariantCombination;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.CreateProductSetCommand;
import com.grab.store.catalog.internal.command.CreateProductSetResult;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.StandaloneVariantDefaults;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductSetCommandHandlerTest {

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String VARIANT_ID = "variant-789";
    private static final String COLOR_TYPE_ID = "color-type-001";
    private static final String RED_OPTION_ID = "red-option-001";
    private static final String RED_MATRIX_KEY = "color:red";

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
    private VariantCombinationService variantCombinationService;
    @Mock
    private VariationCombinationManager variationCombinationManager;
    @Mock
    private VariationKeyGenerator variationKeyGenerator;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @InjectMocks
    private CreateProductSetCommandHandler handler;

    @Test
    void handle_withVariantTypesMaterializesMatchingMatrixVariants() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Id colorTypeId = new CommonId(COLOR_TYPE_ID);
        Id redOptionId = new CommonId(RED_OPTION_ID);
        ProductVariation redVariation = new ProductVariation(redOptionId, colorTypeId);

        CreateProductSetCommand command = new CreateProductSetCommand(
                new CreateProductSetCommand.Product(
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
                        List.of(new CreateProductSetCommand.Variant(
                                "SKU-RED-001",
                                RED_MATRIX_KEY,
                                List.of(new CreateProductSetCommand.Variation(redOptionId, colorTypeId))
                        ))
                ),
                List.of(new CreateProductSetCommand.VariantType(
                        COLOR_TYPE_ID,
                        List.of(new CreateProductSetCommand.VariantOption(RED_OPTION_ID))
                ))
        );

        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(Category.createRoot(categoryId, "Category")));
        when(uniqueSlugResolver.resolve(null, "Product with Variants", null)).thenReturn("product-with-variants");
        when(idGenerator.generateId()).thenReturn(productId, variantId);
        when(idGenerator.convertIdFrom(anyString())).thenAnswer(invocation -> new CommonId(invocation.getArgument(0, String.class)));
        when(variantCombinationService.generateCombinations(anyList())).thenReturn(List.of(
                List.of(new VariantOptionSelection(redOptionId, colorTypeId))
        ));
        when(variationCombinationManager.syncCombinations(anyList(), anyList())).thenReturn(List.of(
                new VariationCombinationManager.VariantCombinationResult(
                        new VariantCombination(List.of(redVariation)),
                        null,
                        VariationCombinationManager.VariantCombinationResult.MatchedType.NEW
                )
        ));
        when(variationKeyGenerator.generateVariationKey(List.of(redVariation))).thenReturn(RED_MATRIX_KEY);

        CreateProductSetResult result = handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(savedProduct.getId()).isEqualTo(productId);
        assertThat(savedProduct.getSlug()).isEqualTo("product-with-variants");
        assertThat(savedProduct.getVariants()).hasSize(1);
        assertThat(savedProduct.getVariants().getFirst().getId()).isEqualTo(variantId);
        assertThat(savedProduct.getVariants().getFirst().getSku()).isEqualTo("SKU-RED-001");
        assertThat(savedProduct.getVariants().getFirst().getStatus()).isEqualTo(ProductVariantStatus.ACTIVE);
        assertThat(savedProduct.getVariants().getFirst().getVariations()).containsExactly(redVariation);
    }

    @Test
    void handle_categoryNotFoundThrows() {
        Id categoryId = new CommonId(CATEGORY_ID);
        CreateProductSetCommand command = new CreateProductSetCommand(
                new CreateProductSetCommand.Product(
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
                ),
                List.of()
        );

        when(categoryRepository.find(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.not_found"));
    }

    @Test
    void handle_noVariantTypes_createsDefaultVariant() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id generatedVariantId = new CommonId(VARIANT_ID);

        CreateProductSetCommand command = new CreateProductSetCommand(
                new CreateProductSetCommand.Product(
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
                ),
                List.of()
        );

        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(Category.createRoot(categoryId, "Category")));
        when(uniqueSlugResolver.resolve(null, "Simple Product", null)).thenReturn("simple-product");
        when(idGenerator.generateId()).thenReturn(productId, generatedVariantId);
        when(idGenerator.convertIdFrom(anyString())).thenAnswer(invocation -> new CommonId(invocation.getArgument(0, String.class)));
        when(skuGenerator.generate(any())).thenReturn("SMP");

        CreateProductSetResult result = handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(savedProduct.getVariants()).hasSize(1);
        assertThat(savedProduct.getVariants().getFirst().getId()).isEqualTo(generatedVariantId);
        assertThat(savedProduct.getVariants().getFirst().getSku()).isEqualTo("SMP");
        assertThat(savedProduct.getVariants().getFirst().getVariations()).hasSize(1);
        assertThat(savedProduct.getVariants().getFirst().getVariations().iterator().next().getTypeId().getValue())
                .isEqualTo(StandaloneVariantDefaults.TYPE_ID);
        assertThat(savedProduct.getVariants().getFirst().getVariations().iterator().next().getOptionId().getValue())
                .isEqualTo(StandaloneVariantDefaults.OPTION_ID);
        assertThat(savedProduct.getVariants().getFirst().getStatus()).isEqualTo(ProductVariantStatus.ACTIVE);
        verifyNoInteractions(variantCombinationService, variationCombinationManager, variationKeyGenerator);
    }

    @Test
    void handle_duplicateMatrixKeysInRequest_throwsIllegalStateException() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id colorTypeId = new CommonId(COLOR_TYPE_ID);
        Id redOptionId = new CommonId(RED_OPTION_ID);

        CreateProductSetCommand command = new CreateProductSetCommand(
                new CreateProductSetCommand.Product(
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
                                new CreateProductSetCommand.Variant(
                                        "SKU-RED-001",
                                        RED_MATRIX_KEY,
                                        List.of(new CreateProductSetCommand.Variation(redOptionId, colorTypeId))
                                ),
                                new CreateProductSetCommand.Variant(
                                        "SKU-RED-002",
                                        RED_MATRIX_KEY,
                                        List.of(new CreateProductSetCommand.Variation(redOptionId, colorTypeId))
                                )
                        )
                ),
                List.of(new CreateProductSetCommand.VariantType(
                        COLOR_TYPE_ID,
                        List.of(new CreateProductSetCommand.VariantOption(RED_OPTION_ID))
                ))
        );

        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(Category.createRoot(categoryId, "Category")));
        when(uniqueSlugResolver.resolve(null, "Product with Variants", null)).thenReturn("product-with-variants");
        when(idGenerator.generateId()).thenReturn(productId);
        when(idGenerator.convertIdFrom(anyString())).thenAnswer(invocation -> new CommonId(invocation.getArgument(0, String.class)));
        when(variantCombinationService.generateCombinations(anyList())).thenReturn(List.of(
                List.of(new VariantOptionSelection(redOptionId, colorTypeId))
        ));
        when(variationCombinationManager.syncCombinations(anyList(), anyList())).thenReturn(List.of());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate key");
    }
}
