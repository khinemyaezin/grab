package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerAllocationController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerAllocationApiLinks {

    private SellerAllocationApiLinks() {
    }

    public static Link allocateStock() {
        return linkTo(methodOn(SellerAllocationController.class).allocateStock(null, null))
                .withRel("allocate-stock");
    }

    public static Link deallocateStock() {
        return linkTo(methodOn(SellerAllocationController.class).deallocateStock(null, null))
                .withRel("deallocate-stock");
    }
}
