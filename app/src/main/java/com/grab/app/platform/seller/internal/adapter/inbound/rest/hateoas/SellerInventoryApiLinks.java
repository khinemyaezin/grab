package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerInventoryController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerInventoryApiLinks {

    private SellerInventoryApiLinks() {
    }

    public static Link getItem(String itemId) {
        return linkTo(methodOn(SellerInventoryController.class).getItem(itemId))
                .withRel("get-inventory-item");
    }

    public static Link getLatestMovement(String itemId) {
        return linkTo(methodOn(SellerInventoryController.class).getLatestMovement(itemId))
                .withRel("get-inventory-movement-latest");
    }

    public static Link createItem() {
        return linkTo(methodOn(SellerInventoryController.class).createItem(null, null, null))
                .withRel("create-inventory-item");
    }
}
