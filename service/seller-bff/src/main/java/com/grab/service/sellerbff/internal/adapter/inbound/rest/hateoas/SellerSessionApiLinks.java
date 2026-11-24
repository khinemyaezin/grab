package com.grab.service.sellerbff.internal.adapter.inbound.rest.hateoas;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.controller.SellerSessionController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerSessionApiLinks {

    private SellerSessionApiLinks() {
    }

    public static Link selectContext(String assignmentId) {
        return linkTo(methodOn(SellerSessionController.class).selectContext(assignmentId))
                .withRel("select-context");
    }

    public static Link switchContext() {
        return linkTo(methodOn(SellerSessionController.class).switchContext())
                .withRel("switch-context");
    }

    public static Link listContexts() {
        return linkTo(methodOn(SellerSessionController.class).listContexts(null))
                .withRel("list-access-contexts");
    }

    public static Link getProfile() {
        return linkTo(methodOn(SellerSessionController.class).getProfile(null))
                .withRel("get-profile");
    }
}
