package com.catalog.application.command.handler;

import com.catalog.application.service.SetVariantMediaService;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.service.impl.DefaultProductMediaService;
import com.grab.framework.id.impl.CommonId;
import com.catalog.application.model.write.SetVariantMediaCommand;
import com.catalog.application.model.write.SetVariantMediaResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SetVariantMediaServiceTest {

    private InMemoryProductRepositoryTest productRepository;
    private SetVariantMediaService service;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepositoryTest();
        service = new SetVariantMediaService(productRepository, new DefaultProductMediaService());
    }

    @Test
    void assignsSubsetOfProductMediaToVariant() {
        seedProduct();

        SetVariantMediaResult result = service.execute(new SetVariantMediaCommand(
                new CommonId("merchant-1"),
                new CommonId("product-1"),
                new CommonId("variant-1"),
                List.of(new CommonId("media-1")),
                new CommonId("media-1")
        ));

        assertThat(result.mediaIds()).containsExactly("media-1");
        assertThat(result.thumbnailMediaId()).isEqualTo("media-1");
        Product saved = productRepository.getLastSaved();
        assertThat(saved.findVariantById(new CommonId("variant-1")).orElseThrow().getMediaIds())
                .extracting(id -> id.getValue())
                .containsExactly("media-1");
    }

    @Test
    void rejectsMediaNotOnProduct() {
        seedProduct();

        assertThatThrownBy(() -> service.execute(new SetVariantMediaCommand(
                new CommonId("merchant-1"),
                new CommonId("product-1"),
                new CommonId("variant-1"),
                List.of(new CommonId("other-media")),
                null
        ))).isInstanceOf(CatalogDomainValidationException.class);
    }

    private void seedProduct() {
        Product product = Product.create(
                new CommonId("product-1"),
                new CommonId("merchant-1"),
                "Camera",
                new CommonId("electronics"),
                null,
                null,
                List.of(),
                List.of(new ProductMedia(
                        new CommonId("media-1"),
                        "merchants/merchant-1/products/product-1/a.jpg",
                        "http://localhost:9000/grab-media/a.jpg",
                        "image/jpeg",
                        0
                ))
        );
        product.addVariant(ProductVariant.create(new CommonId("variant-1"), "SKU-1", List.of()));
        productRepository.put(product);
    }
}
