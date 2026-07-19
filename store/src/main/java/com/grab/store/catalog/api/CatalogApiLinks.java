package com.grab.store.catalog.api;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class CatalogApiLinks {

    private CatalogApiLinks() {
    }

    public static Link searchProductVariants() {
        return linkTo(methodOn(ProductController.class).searchProductVariants(null, null, null))
                .withRel("search-product-variants");
    }
}
