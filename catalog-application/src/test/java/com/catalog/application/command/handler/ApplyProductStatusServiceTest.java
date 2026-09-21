package com.catalog.application.command.handler;

import com.catalog.application.service.ApplyProductStatusService;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.catalog.application.model.write.ApplyProductStatusCommand;
import com.catalog.application.model.write.ApplyProductStatusResult;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyProductStatusServiceTest {

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String VARIANT_ID = "variant-abc";

    @Mock
    private ProductRepository productRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private ApplyProductStatusService service;

    @BeforeEach
    void setUp() {
        service = new ApplyProductStatusService(productRepository);
    }

    @Test
    void handle_whenListingComplete_activatesProduct() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = publishableProduct(productId);
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        ApplyProductStatusResult result = service.execute(new ApplyProductStatusCommand(
                productId,
                productId,
                "ACTIVE"
        ));

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    void handle_whenListingIncomplete_failsWithoutSwallowing() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = Product.create(productId, productId, "Old Name", new CommonId(CATEGORY_ID));
        product.addVariant(ProductVariant.create(
                new CommonId(VARIANT_ID),
                "SKU-1",
                List.of(new ProductVariation(new CommonId("opt-1"), new CommonId("color")))
        ));
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.execute(new ApplyProductStatusCommand(
                productId,
                productId,
                "ACTIVE"
        )))
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> {
                    CatalogDomainValidationException typed = (CatalogDomainValidationException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.domain.listing_incomplete");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
        verify(productRepository, never()).save(product);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void handle_whenStatusBlank_isNoOp() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = publishableProduct(productId);
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        ApplyProductStatusResult result = service.execute(new ApplyProductStatusCommand(
                productId,
                productId,
                " "
        ));

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(result.status()).isEqualTo("DRAFT");
    }

    @Test
    void handle_whenStatusSame_isNoOp() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = publishableProduct(productId);
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        ApplyProductStatusResult result = service.execute(new ApplyProductStatusCommand(
                productId,
                productId,
                "DRAFT"
        ));

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(result.status()).isEqualTo("DRAFT");
    }

    @Test
    void handle_productNotFoundThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        when(productRepository.find(productId, productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ApplyProductStatusCommand(
                productId,
                productId,
                "ACTIVE"
        )))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.product.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    private Product publishableProduct(Id productId) {
        Product product = Product.create(
                productId,
                productId,
                "Old Name",
                new CommonId(CATEGORY_ID),
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
                new CommonId(VARIANT_ID),
                "SKU-1",
                List.of(new ProductVariation(new CommonId("opt-1"), new CommonId("color")))
        ));
        return product;
    }
}
