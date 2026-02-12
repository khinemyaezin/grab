package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.aggregate.ProductVariantStatus;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.assembler.CommonId;
import com.grab.store.catalog.internal.command.DeleteVariantCommand;
import com.grab.store.catalog.internal.command.DeleteVariantResult;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteVariantCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private DeleteVariantCommandHandler handler;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String VARIANT_ID = "variant-789";

    @BeforeEach
    void setUp() {
        handler = new DeleteVariantCommandHandler(productRepository);
    }

    @Test
    void handle_softDeletesActiveVariant() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                "Red", new CommonId("opt-red"), "Color", new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation));
        product.addVariant(variant);

        when(productRepository.find(productId)).thenReturn(Optional.of(product));

        DeleteVariantCommand command = new DeleteVariantCommand(productId, variantId);
        DeleteVariantResult result = handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();

        assertThat(saved.findVariantById(variantId))
                .isPresent()
                .hasValueSatisfying(v -> assertThat(v.getStatus()).isEqualTo(ProductVariantStatus.DELETED));

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.variantId()).isEqualTo(VARIANT_ID);
        assertThat(result.deleted()).isTrue();
    }

    @Test
    void handle_productNotFound_returnsFalse() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);
        when(productRepository.find(productId)).thenReturn(Optional.empty());

        DeleteVariantCommand command = new DeleteVariantCommand(productId, variantId);
        DeleteVariantResult result = handler.handle(command);

        assertThat(result.deleted()).isFalse();
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.variantId()).isEqualTo(VARIANT_ID);
    }

    @Test
    void handle_variantAlreadyDeleted_remainsDeleted() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                "Red", new CommonId("opt-red"), "Color", new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation));
        product.addVariant(variant);
        variant.markAsDeleted();

        when(productRepository.find(productId)).thenReturn(Optional.of(product));

        DeleteVariantCommand command = new DeleteVariantCommand(productId, variantId);
        DeleteVariantResult result = handler.handle(command);

        assertThat(result.deleted()).isTrue();
    }
}
