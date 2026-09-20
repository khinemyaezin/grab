package com.catalog.application.command.handler;

import com.catalog.application.service.UpdateVariantService;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.catalog.application.model.write.UpdateVariantCommand;
import com.catalog.application.model.write.UpdateVariantResult;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateVariantServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private UpdateVariantService service;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String VARIANT_ID = "variant-789";

    @BeforeEach
    void setUp() {
        service = new UpdateVariantService(productRepository);
    }

    @Test
    void handle_updatesSku() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, productId, "Product", categoryId);
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "OLD-SKU", List.of(variation));
        product.addVariant(variant);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        UpdateVariantCommand command = new UpdateVariantCommand(productId, productId, variantId, "NEW-SKU", null);
        UpdateVariantResult result = service.execute(command);

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
    void handle_updatesSku_preservesVariantMedia() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Id mediaId = new CommonId("media-1");

        Product product = Product.create(
                productId,
                productId,
                "Product",
                new CommonId(CATEGORY_ID),
                null,
                null,
                List.of(),
                List.of(new ProductMedia(
                        mediaId,
                        "merchants/m/products/p/1.jpg",
                        "http://minio/1.jpg",
                        "image/jpeg",
                        0
                ))
        );
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = new ProductVariant(
                variantId,
                "OLD-SKU",
                ProductVariantStatus.ACTIVE,
                List.of(variation),
                false,
                List.of(mediaId),
                mediaId
        );
        product.addVariant(variant);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        service.execute(new UpdateVariantCommand(productId, productId, variantId, "NEW-SKU", null));

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();
        assertThat(saved.findVariantById(variantId))
                .isPresent()
                .hasValueSatisfying(v -> {
                    assertThat(v.getSku()).isEqualTo("NEW-SKU");
                    assertThat(v.getMediaIds()).extracting(Id::getValue).containsExactly("media-1");
                    assertThat(v.getThumbnailMediaId().getValue()).isEqualTo("media-1");
                });
    }

    @Test
    void handle_productNotFoundThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        when(productRepository.find(productId, productId)).thenReturn(Optional.empty());

        UpdateVariantCommand command = new UpdateVariantCommand(productId, productId, new CommonId(VARIANT_ID), "SKU", null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.product.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    @Test
    void handle_variantNotFoundThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = Product.create(productId, productId, "Product", new CommonId(CATEGORY_ID));
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        UpdateVariantCommand command = new UpdateVariantCommand(productId, productId, new CommonId(VARIANT_ID), "SKU", null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.variant.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    @Test
    void handle_deletedVariantThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation));
        product.addVariant(variant);
        variant.markAsDeleted();

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        UpdateVariantCommand command = new UpdateVariantCommand(productId, productId, variantId, "NEW-SKU", null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.variant.deleted_cannot_update");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }

    @Test
    void handle_duplicateSkuThrows() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation));
        product.addVariant(variant);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));
        when(productRepository.isSkuTaken(productId, "NEW-SKU", VARIANT_ID)).thenReturn(true);

        UpdateVariantCommand command = new UpdateVariantCommand(productId, productId, variantId, "NEW-SKU", null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.variant.sku_already_exists");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.CONFLICT);
                });
    }

    @Test
    void handle_sameSku_skipsSkuAvailabilityValidation() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);

        Product product = Product.create(productId, productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SAME-SKU", List.of(variation));
        product.addVariant(variant);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        UpdateVariantCommand command = new UpdateVariantCommand(productId, productId, variantId, "SAME-SKU", null);
        UpdateVariantResult result = service.execute(command);

        verify(productRepository, never()).isSkuTaken(any(), any(), any());
        verify(productRepository).save(productCaptor.capture());
        assertThat(result.sku()).isEqualTo("SAME-SKU");
    }

    @Test
    void handle_updatesManageInventory() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Product product = productWithVariant(productId, variantId, false);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        service.execute(new UpdateVariantCommand(productId, productId, variantId, "SKU-1", true));

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().findVariantById(variantId))
                .isPresent()
                .hasValueSatisfying(variant -> assertThat(variant.isManageInventory()).isTrue());
    }

    @Test
    void handle_clearsManageInventory() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Product product = productWithVariant(productId, variantId, true);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        service.execute(new UpdateVariantCommand(productId, productId, variantId, "SKU-1", false));

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().findVariantById(variantId))
                .isPresent()
                .hasValueSatisfying(variant -> assertThat(variant.isManageInventory()).isFalse());
    }

    @Test
    void handle_nullManageInventory_preservesExisting() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Product product = productWithVariant(productId, variantId, true);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        service.execute(new UpdateVariantCommand(productId, productId, variantId, "NEW-SKU", null));

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().findVariantById(variantId))
                .isPresent()
                .hasValueSatisfying(variant -> {
                    assertThat(variant.getSku()).isEqualTo("NEW-SKU");
                    assertThat(variant.isManageInventory()).isTrue();
                });
    }

    private static Product productWithVariant(Id productId, Id variantId, boolean manageInventory) {
        Product product = Product.create(productId, productId, "Product", new CommonId(CATEGORY_ID));
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "SKU-1", List.of(variation), manageInventory);
        product.addVariant(variant);
        return product;
    }
}
