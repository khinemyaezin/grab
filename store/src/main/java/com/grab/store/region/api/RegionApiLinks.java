package com.grab.store.region.api;

import com.grab.store.region.internal.api.rest.controller.RegionController;
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
}
