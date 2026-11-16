package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerVariantOptionController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerVariantOptionApiLinks {

    private SellerVariantOptionApiLinks() {
    }

    public static Link listVariantOptions(String name, String typeId) {
        return linkTo(methodOn(SellerVariantOptionController.class).listVariantOptions(name, typeId))
                .withRel("search-variant-options");
    }

    public static Link listVariantTypes(String name) {
        return linkTo(methodOn(SellerVariantOptionController.class).listVariantTypes(name))
                .withRel("search-variant-types");
    }
}
