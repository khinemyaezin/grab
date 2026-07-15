package com.grab.store.catalog.api;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogApiLinksTest {

    @Test
    void searchProducts_shouldUseCatalogSearchRelAndHref() {
        Link link = CatalogApiLinks.searchProducts();

        assertThat(link.getRel().value()).isEqualTo("search-products");
        assertThat(link.getHref()).endsWith("/api/v1/catalog/products/search");
    }

    @Test
    void getProduct_shouldUseCatalogProductDetailRelAndTemplatedHref() {
        Link link = CatalogApiLinks.getProduct();

        assertThat(link.getRel().value()).isEqualTo("get-product");
        assertThat(link.getHref()).contains("/api/v1/catalog/products/");
        assertThat(link.getHref()).contains("{productId}");
    }
}
