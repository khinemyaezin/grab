package com.catalog.application.command.handler;

import com.catalog.application.service.RestoreVariantService;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.event.ProductVariantRestoredEvent;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.catalog.application.model.write.RestoreVariantCommand;
import com.catalog.application.model.write.RestoreVariantResult;
import com.catalog.application.exception.CatalogServiceException;
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
class RestoreVariantServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private RestoreVariantService service;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String VARIANT_ID = "variant-789";

    @BeforeEach
    void setUp() {
        service = new RestoreVariantService(productRepository);
    }

    @Test
    void handle_restoresDeletedVariant() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation));
        product.addVariant(variant);
        variant.markAsDeleted();

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        RestoreVariantCommand command = new RestoreVariantCommand(productId, productId, variantId);
        RestoreVariantResult result = service.execute(command);

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();

        assertThat(saved.findVariantById(variantId))
                .isPresent()
                .hasValueSatisfying(v -> assertThat(v.getStatus()).isEqualTo(ProductVariantStatus.ACTIVE));
        assertThat(saved.getEvents()).anyMatch(ProductVariantRestoredEvent.class::isInstance);

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.variantId()).isEqualTo(VARIANT_ID);
        assertThat(result.status()).isEqualTo(ProductVariantStatus.ACTIVE.name());
    }

    @Test
    void handle_productNotFoundThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);
        when(productRepository.find(productId, productId)).thenReturn(Optional.empty());

        RestoreVariantCommand command = new RestoreVariantCommand(productId, productId, variantId);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.product.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    @Test
    void handle_variantNotDeletedOrNotFoundThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation));
        product.addVariant(variant);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        RestoreVariantCommand command = new RestoreVariantCommand(productId, productId, variantId);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.variant.not_found_or_not_deleted");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }
}
