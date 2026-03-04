package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.aggregate.ProductVariantStatus;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.UpdateVariantCommand;
import com.grab.store.catalog.internal.command.UpdateVariantResult;
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
class UpdateVariantCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private UpdateVariantCommandHandler handler;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String VARIANT_ID = "variant-789";

    @BeforeEach
    void setUp() {
        handler = new UpdateVariantCommandHandler(productRepository);
    }

    @Test
    void handle_updatesSku() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, "Product", categoryId);
        ProductVariation variation = new ProductVariation(
                "Red", new CommonId("opt-red"), "Color", new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "OLD-SKU", List.of(variation));
        product.addVariant(variant);

        when(productRepository.find(productId)).thenReturn(Optional.of(product));

        UpdateVariantCommand command = new UpdateVariantCommand(productId, variantId, "NEW-SKU");
        UpdateVariantResult result = handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();

        assertThat(saved.findVariantById(variantId))
                .isPresent()
                .hasValueSatisfying(v -> assertThat(v.getSku()).isEqualTo("NEW-SKU"));

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.variantId()).isEqualTo(VARIANT_ID);
        assertThat(result.sku()).isEqualTo("NEW-SKU");
        assertThat(result.status()).isEqualTo(ProductVariantStatus.ACTIVE.name());
    }

    @Test
    void handle_productNotFound_throws() {
        Id productId = new CommonId(PRODUCT_ID);
        when(productRepository.find(productId)).thenReturn(Optional.empty());

        UpdateVariantCommand command = new UpdateVariantCommand(productId, new CommonId(VARIANT_ID), "SKU");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handle_variantNotFound_throws() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = Product.create(productId, "Product", new CommonId(CATEGORY_ID));
        when(productRepository.find(productId)).thenReturn(Optional.of(product));

        UpdateVariantCommand command = new UpdateVariantCommand(productId, new CommonId(VARIANT_ID), "SKU");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handle_deletedVariant_throws() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                "Red", new CommonId("opt-red"), "Color", new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation));
        product.addVariant(variant);
        variant.markAsDeleted();

        when(productRepository.find(productId)).thenReturn(Optional.of(product));

        UpdateVariantCommand command = new UpdateVariantCommand(productId, variantId, "NEW-SKU");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalStateException.class);
    }
}
