package com.catalog.domain.service.impl;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.service.ProductMediaService;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultProductMediaServiceTest {

    private ProductMediaService productMediaService;

    @BeforeEach
    void setUp() {
        productMediaService = new DefaultProductMediaService();
    }

    @Test
    void replaceGallery_rejectsDuplicateStorageKey() {
        Product product = productWithMedia();

        assertThatThrownBy(() -> productMediaService.replaceGallery(product, List.of(
                new ProductMedia(new CommonId("media-1"), "same.jpg", "http://minio/1.jpg", "image/jpeg", 0),
                new ProductMedia(new CommonId("media-2"), "same.jpg", "http://minio/2.jpg", "image/jpeg", 1)
        )))
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> assertThat(((CatalogDomainValidationException) exception).getMessageSource().code())
                        .isEqualTo("cat.domain.duplicate_product_media"));
    }

    @Test
    void replaceGallery_rejectsDuplicateMediaId() {
        Product product = productWithMedia();

        assertThatThrownBy(() -> productMediaService.replaceGallery(product, List.of(
                new ProductMedia(new CommonId("media-1"), "a.jpg", "http://minio/1.jpg", "image/jpeg", 0),
                new ProductMedia(new CommonId("media-1"), "b.jpg", "http://minio/2.jpg", "image/jpeg", 1)
        )))
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> assertThat(((CatalogDomainValidationException) exception).getMessageSource().code())
                        .isEqualTo("cat.domain.duplicate_product_media"));
    }

    @Test
    void assignVariantMedia_acceptsSubsetOfProductMedia() {
        Product product = productWithMedia();
        product.addVariant(variant("v1", "SKU-1", "red"));

        productMediaService.assignVariantMedia(
                product,
                new CommonId("v1"),
                List.of(new CommonId("media-1")),
                new CommonId("media-1")
        );

        ProductVariant stored = product.findVariantById(new CommonId("v1")).orElseThrow();
        assertThat(stored.getMediaIds()).extracting(id -> id.getValue()).containsExactly("media-1");
        assertThat(stored.getThumbnailMediaId().getValue()).isEqualTo("media-1");
    }

    @Test
    void assignVariantMedia_includesThumbnailInSubset() {
        Product product = productWithMedia();
        product.addVariant(variant("v1", "SKU-1", "red"));

        productMediaService.assignVariantMedia(product, new CommonId("v1"), List.of(), new CommonId("media-1"));

        ProductVariant stored = product.findVariantById(new CommonId("v1")).orElseThrow();
        assertThat(stored.getMediaIds()).extracting(id -> id.getValue()).containsExactly("media-1");
        assertThat(stored.getThumbnailMediaId().getValue()).isEqualTo("media-1");
    }

    @Test
    void assignVariantMedia_rejectsMediaNotOwnedByProduct() {
        Product product = productWithMedia();
        product.addVariant(variant("v1", "SKU-1", "red"));

        assertThatThrownBy(() -> productMediaService.assignVariantMedia(
                product,
                new CommonId("v1"),
                List.of(new CommonId("foreign-media")),
                null
        ))
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> assertThat(((CatalogDomainValidationException) exception).getMessageSource().code())
                        .isEqualTo("cat.domain.variant_media_not_owned_by_product"));
    }

    private static Product productWithMedia() {
        return Product.create(
                new CommonId("product-1"),
                new CommonId("merchant-1"),
                "T-Shirt",
                new CommonId("clothing"),
                null,
                null,
                List.of(),
                List.of(new ProductMedia(
                        new CommonId("media-1"),
                        "merchants/m/products/p/1.jpg",
                        "http://minio/1.jpg",
                        "image/jpeg",
                        0
                ))
        );
    }

    private static ProductVariant variant(String id, String sku, String optionId) {
        return ProductVariant.create(
                new CommonId(id),
                sku,
                List.of(new ProductVariation(new CommonId(optionId), new CommonId("color")))
        );
    }
}
