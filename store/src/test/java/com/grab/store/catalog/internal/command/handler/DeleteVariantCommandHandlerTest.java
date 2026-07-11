package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.event.ProductVariantDeletedEvent;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        Product product = Product.create(productId, productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation));
        product.addVariant(variant);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        DeleteVariantCommand command = new DeleteVariantCommand(productId, productId, variantId);
        DeleteVariantResult result = handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();

        assertThat(saved.findVariantById(variantId))
                .isPresent()
                .hasValueSatisfying(v -> assertThat(v.getStatus()).isEqualTo(ProductVariantStatus.DELETED));
        assertThat(saved.getEvents()).anyMatch(ProductVariantDeletedEvent.class::isInstance);

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.variantId()).isEqualTo(VARIANT_ID);
        assertThat(result.deleted()).isTrue();
    }

    @Test
    void handle_productNotFoundReturnsFalse() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);
        when(productRepository.find(productId, productId)).thenReturn(Optional.empty());

        DeleteVariantCommand command = new DeleteVariantCommand(productId, productId, variantId);
        DeleteVariantResult result = handler.handle(command);

        assertThat(result.deleted()).isFalse();
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.variantId()).isEqualTo(VARIANT_ID);
    }

    @Test
    void handle_variantAlreadyDeletedRemainsDeleted() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation));
        product.addVariant(variant);
        variant.markAsDeleted();

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        DeleteVariantCommand command = new DeleteVariantCommand(productId, productId, variantId);
        DeleteVariantResult result = handler.handle(command);

        assertThat(result.deleted()).isTrue();
    }

    @Test
    void handle_lastActiveVariantOnActiveProductThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(
                productId,
                productId,
                "Product",
                new CommonId(CATEGORY_ID),
                null,
                null,
                List.of(new Description(null, "summary", "Summary", "Product summary")),
                List.of(new ProductMedia(null, "IMAGE", "/images/product.png"))
        );
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation));
        product.addVariant(variant);
        product.changeStatus(ProductStatus.ACTIVE);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        DeleteVariantCommand command = new DeleteVariantCommand(productId, productId, variantId);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> assertThat(((CatalogDomainValidationException) exception).getMessageSource().code())
                        .isEqualTo("cat.domain.cannot_delete_last_active_variant_from_active_product"));
    }
}
