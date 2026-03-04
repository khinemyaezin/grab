package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductStatus;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.aggregate.ProductVariantStatus;
import com.catalog.domain.exception.InvalidProductStatusTransitionException;
import com.catalog.domain.exception.ProductActivationRequiresActiveVariantsException;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.UpdateProductStatusCommand;
import com.grab.store.catalog.internal.command.UpdateProductStatusResult;
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

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private UpdateProductStatusCommandHandler handler;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";

    @BeforeEach
    void setUp() {
        handler = new UpdateProductStatusCommandHandler(productRepository);
    }

    @Test
    void handle_draftToActive_withActiveVariants() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = Product.create(productId, "Product", new CommonId(CATEGORY_ID));
        addActiveVariant(product, "v1");

        when(productRepository.find(productId)).thenReturn(Optional.of(product));

        UpdateProductStatusCommand command = new UpdateProductStatusCommand(productId, "ACTIVE");
        UpdateProductStatusResult result = handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();

        assertThat(saved.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.oldStatus()).isEqualTo("DRAFT");
        assertThat(result.newStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void handle_draftToActive_withoutActiveVariants_throws() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = Product.create(productId, "Product", new CommonId(CATEGORY_ID));
        when(productRepository.find(productId)).thenReturn(Optional.of(product));

        UpdateProductStatusCommand command = new UpdateProductStatusCommand(productId, "ACTIVE");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ProductActivationRequiresActiveVariantsException.class);
    }

    @Test
    void handle_invalidTransition_throws() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = Product.create(productId, "Product", new CommonId(CATEGORY_ID));
        when(productRepository.find(productId)).thenReturn(Optional.of(product));

        UpdateProductStatusCommand command = new UpdateProductStatusCommand(productId, "ARCHIVED");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InvalidProductStatusTransitionException.class);
    }

    @Test
    void handle_productNotFound_throws() {
        Id productId = new CommonId(PRODUCT_ID);
        when(productRepository.find(productId)).thenReturn(Optional.empty());

        UpdateProductStatusCommand command = new UpdateProductStatusCommand(productId, "ACTIVE");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void addActiveVariant(Product product, String variantIdValue) {
        Id variantId = new CommonId(variantIdValue);
        ProductVariation variation = new ProductVariation(
                "Red", new CommonId("opt-red"), "Color", new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-" + variantIdValue, List.of(variation));
        product.addVariant(variant);
    }
}
