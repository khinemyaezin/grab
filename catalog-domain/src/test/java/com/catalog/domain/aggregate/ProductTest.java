package com.catalog.domain.aggregate;

import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void removeVariant_lastActive_throwsRegardlessOfProductStatus() {
        for (ProductStatus status : List.of(ProductStatus.DRAFT, ProductStatus.ACTIVE, ProductStatus.ARCHIVED)) {
            Product product = productWithStatus(status, variant("v1", "SKU-1", "red"));

            assertThatThrownBy(() -> product.removeVariant(new CommonId("v1")))
                    .isInstanceOf(CatalogDomainValidationException.class)
                    .satisfies(exception -> assertThat(((CatalogDomainValidationException) exception).getMessageSource().code())
                            .isEqualTo("cat.domain.cannot_delete_last_active_variant"));
            assertThat(product.findVariantById(new CommonId("v1"))).isPresent();
        }
    }

    @Test
    void applySoftDeleteVariants_lastActive_throwsRegardlessOfProductStatus() {
        for (ProductStatus status : List.of(ProductStatus.DRAFT, ProductStatus.ACTIVE, ProductStatus.ARCHIVED)) {
            Product product = productWithStatus(status, variant("v1", "SKU-1", "red"));

            assertThatThrownBy(() -> product.applySoftDeleteVariants(Set.of(new CommonId("v1"))))
                    .isInstanceOf(CatalogDomainValidationException.class)
                    .satisfies(exception -> assertThat(((CatalogDomainValidationException) exception).getMessageSource().code())
                            .isEqualTo("cat.domain.cannot_delete_last_active_variant"));
            assertThat(product.getActiveVariants()).hasSize(1);
        }
    }

    @Test
    void removeVariant_oneOfSeveralActiveVariants_succeeds() {
        Product product = productWithStatus(
                ProductStatus.DRAFT,
                variant("v1", "SKU-1", "red"),
                variant("v2", "SKU-2", "blue")
        );

        product.removeVariant(new CommonId("v1"));

        assertThat(product.findVariantById(new CommonId("v1"))).isEmpty();
        assertThat(product.getActiveVariants()).extracting(v -> v.getId().getValue()).containsExactly("v2");
    }

    @Test
    void applySoftDeleteVariants_oneOfSeveralActiveVariants_succeeds() {
        Product product = productWithStatus(
                ProductStatus.DRAFT,
                variant("v1", "SKU-1", "red"),
                variant("v2", "SKU-2", "blue")
        );

        product.applySoftDeleteVariants(Set.of(new CommonId("v1")));

        assertThat(product.findVariantById(new CommonId("v1")))
                .isPresent()
                .hasValueSatisfying(ProductVariant::isDeleted);
        assertThat(product.getActiveVariants()).extracting(v -> v.getId().getValue()).containsExactly("v2");
    }

    @Test
    void removeVariant_alreadyDeletedWhenAnotherActiveRemains_doesNotThrow() {
        Product product = productWithStatus(
                ProductStatus.DRAFT,
                variant("v1", "SKU-1", "red"),
                variant("v2", "SKU-2", "blue")
        );
        product.findVariantById(new CommonId("v2")).orElseThrow().markAsDeleted();

        product.removeVariant(new CommonId("v2"));

        assertThat(product.findVariantById(new CommonId("v2"))).isEmpty();
        assertThat(product.getActiveVariants()).extracting(v -> v.getId().getValue()).containsExactly("v1");
    }

    @Test
    void replaceMedias_prunesVariantMediaNoLongerOwned() {
        Product product = productWithMedia();
        product.addVariant(variant("v1", "SKU-1", "red"));
        product.applyVariantMedia(new CommonId("v1"), List.of(new CommonId("media-1")), new CommonId("media-1"));

        product.replaceMedias(List.of(new ProductMedia(
                new CommonId("media-2"),
                "merchants/m/products/p/2.jpg",
                "http://minio/2.jpg",
                "image/jpeg",
                0
        )));

        ProductVariant stored = product.findVariantById(new CommonId("v1")).orElseThrow();
        assertThat(stored.getMediaIds()).isEmpty();
        assertThat(stored.getThumbnailMediaId()).isNull();
    }

    @Test
    void publish_missingDescriptions_throwsListingIncomplete() {
        Product product = Product.create(
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
        product.addVariant(variant("v1", "SKU-1", "red"));

        assertThatThrownBy(product::publish)
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> assertThat(((CatalogDomainValidationException) exception).getMessageSource().code())
                        .isEqualTo("cat.domain.listing_incomplete"));
    }

    @Test
    void publish_missingMedias_throwsListingIncomplete() {
        Product product = Product.create(
                new CommonId("product-1"),
                new CommonId("merchant-1"),
                "T-Shirt",
                new CommonId("clothing"),
                null,
                null,
                List.of(Description.create(new CommonId("desc-1"), "Default", "Title", "A description")),
                List.of()
        );
        product.addVariant(variant("v1", "SKU-1", "red"));

        assertThatThrownBy(product::publish)
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> assertThat(((CatalogDomainValidationException) exception).getMessageSource().code())
                        .isEqualTo("cat.domain.listing_incomplete"));
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

    private static Product productWithStatus(ProductStatus status, ProductVariant... variants) {
        Product product = Product.create(
                new CommonId("product-1"),
                new CommonId("merchant-1"),
                "T-Shirt",
                new CommonId("clothing"),
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
        for (ProductVariant variant : variants) {
            product.addVariant(variant);
        }
        if (status != ProductStatus.DRAFT) {
            product.changeStatus(status);
        }
        return product;
    }

    private static ProductVariant variant(String id, String sku, String optionId) {
        return ProductVariant.create(
                new CommonId(id),
                sku,
                List.of(new ProductVariation(new CommonId(optionId), new CommonId("color")))
        );
    }
}
