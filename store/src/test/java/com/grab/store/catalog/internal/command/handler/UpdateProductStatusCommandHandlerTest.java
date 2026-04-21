package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.event.ProductStatusChangedEvent;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.SellerType;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.UpdateProductStatusCommand;
import com.grab.store.catalog.internal.command.UpdateProductStatusResult;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductStatusCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private UpdateProductStatusCommandHandler handler;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";

    @BeforeEach
    void setUp() {
        handler = new UpdateProductStatusCommandHandler(productRepository, categoryRepository);
    }

    @Test
    void handle_draftToActiveWithActiveVariants() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = createPublishableProduct(productId);
        addActiveVariant(product, "v1");

        when(productRepository.find(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.find(new CommonId(CATEGORY_ID)))
                .thenReturn(Optional.of(com.catalog.domain.aggregate.Category.createRoot(new CommonId(CATEGORY_ID), "Category")));

        UpdateProductStatusCommand command = new UpdateProductStatusCommand(productId, "ACTIVE");
        UpdateProductStatusResult result = handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();

        assertThat(saved.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(saved.getEvents()).anyMatch(ProductStatusChangedEvent.class::isInstance);
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.oldStatus()).isEqualTo("DRAFT");
        assertThat(result.newStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void handle_draftToActiveWithoutActiveVariantsThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = createPublishableProduct(productId);
        when(productRepository.find(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.find(new CommonId(CATEGORY_ID)))
                .thenReturn(Optional.of(com.catalog.domain.aggregate.Category.createRoot(new CommonId(CATEGORY_ID), "Category")));

        UpdateProductStatusCommand command = new UpdateProductStatusCommand(productId, "ACTIVE");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> {
                    CatalogDomainValidationException typed = (CatalogDomainValidationException) exception;
                    assertThat(typed.getMessageSource().code())
                            .isEqualTo("cat.domain.product_activation_requires_active_variants");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }

    @Test
    void handle_invalidTransitionThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = createPublishableProduct(productId);
        addActiveVariant(product, "v1");
        product.changeStatus(ProductStatus.ACTIVE);
        when(productRepository.find(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.find(new CommonId(CATEGORY_ID)))
                .thenReturn(Optional.of(com.catalog.domain.aggregate.Category.createRoot(new CommonId(CATEGORY_ID), "Category")));

        UpdateProductStatusCommand command = new UpdateProductStatusCommand(productId, "DRAFT");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> {
                    CatalogDomainValidationException typed = (CatalogDomainValidationException) exception;
                    assertThat(typed.getMessageSource().code())
                            .isEqualTo("cat.domain.invalid_product_status_transition");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }

    @Test
    void handle_productNotFoundThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        when(productRepository.find(productId)).thenReturn(Optional.empty());

        UpdateProductStatusCommand command = new UpdateProductStatusCommand(productId, "ACTIVE");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.product.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    private void addActiveVariant(Product product, String variantIdValue) {
        Id variantId = new CommonId(variantIdValue);
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-" + variantIdValue, List.of(variation));
        product.addVariant(variant);
    }

    private Product createPublishableProduct(Id productId) {
        return Product.create(
                productId,
                "Product",
                new CommonId(CATEGORY_ID),
                null,
                "product",
                List.of(new Description(null, "default", "Product", "Description")),
                List.of(new ProductMedia(null, "IMAGE", "/images/product.jpg"))
        );
    }
}
