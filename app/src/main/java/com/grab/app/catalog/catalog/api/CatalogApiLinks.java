package com.grab.app.catalog.catalog.api;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.ProductController;
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
