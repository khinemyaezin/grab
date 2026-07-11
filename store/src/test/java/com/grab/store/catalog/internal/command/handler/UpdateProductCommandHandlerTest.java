package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.MatrixCombinationService;
import com.catalog.domain.service.MatrixCombinationSynchronizer;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.UpdateProductCommand;
import com.grab.store.catalog.internal.command.UpdateProductResult;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductCommandHandlerTest {

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

    private UpdateProductCommandHandler handler;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String NEW_CATEGORY_ID = "category-789";

    @BeforeEach
    void setUp() {
        handler = new UpdateProductCommandHandler(
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
        UpdateProductResult result = handler.handle(command);

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

        assertThatThrownBy(() -> handler.handle(command))
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

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.category.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }
}
