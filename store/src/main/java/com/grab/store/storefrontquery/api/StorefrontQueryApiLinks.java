package com.grab.store.storefrontquery.api;

import com.grab.store.storefrontquery.internal.api.rest.controller.StorefrontProductController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class StorefrontQueryApiLinks {

    private StorefrontQueryApiLinks() {
    }

    public static Link listStorefrontProducts() {
        return linkTo(methodOn(StorefrontProductController.class).list(null, null, null))
                .withRel("list-storefront-products");
    }

    public static Link getStorefrontProduct() {
        return linkTo(methodOn(StorefrontProductController.class).get(null, null))
                .withRel("get-storefront-product");
    }
}
