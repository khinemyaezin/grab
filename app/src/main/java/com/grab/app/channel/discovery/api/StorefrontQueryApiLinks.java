package com.grab.app.channel.discovery.api;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.StorefrontProductController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class StorefrontQueryApiLinks {

    private StorefrontQueryApiLinks() {
    }

    public static Link listStorefrontProducts() {
        return linkTo(methodOn(StorefrontProductController.class).list(null, null, null, null, null, null, null))
                .withRel("list-storefront-products");
    }

    public static Link getStorefrontProduct() {
        return linkTo(methodOn(StorefrontProductController.class).getBySlug(null, null, null, null))
                .withRel("get-storefront-product");
    }

    public static Link resolveShoppingContext() {
        return linkTo(methodOn(StorefrontProductController.class).resolveContext(null, null, null))
                .withRel("resolve-shopping-context");
    }
}
