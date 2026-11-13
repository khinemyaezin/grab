package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerRegionController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerRegionApiLinks {

    private SellerRegionApiLinks() {
    }

    public static Link get(String regionId) {
        return linkTo(methodOn(SellerRegionController.class).get(regionId))
                .withRel("get-region");
    }

    public static Link countries() {
        return linkTo(methodOn(SellerRegionController.class).countries())
                .withRel("list-region-countries");
    }
}
