package com.grab.app.channel.region.api;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.RegionController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class RegionApiLinks {

    private RegionApiLinks() {
    }

    public static Link getRegion() {
        return linkTo(methodOn(RegionController.class).get(null))
                .withRel("get-region");
    }

    public static Link listRegionCountries() {
        return linkTo(methodOn(RegionController.class).countries())
                .withRel("list-region-countries");
    }
}
