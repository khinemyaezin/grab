package com.grab.store.catalog.api;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogApiLinksTest {

    @Test
    void searchProductVariants_shouldUseCatalogVariantSearchRelAndHref() {
        Link link = CatalogApiLinks.searchProductVariants();

        assertThat(link.getRel().value()).isEqualTo("search-product-variants");
        assertThat(link.getHref()).endsWith("/api/v1/catalog/products/variants/search");
    }
}
