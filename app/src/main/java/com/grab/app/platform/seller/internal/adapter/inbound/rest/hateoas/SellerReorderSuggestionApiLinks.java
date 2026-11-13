package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerReorderSuggestionController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerReorderSuggestionApiLinks {

    private SellerReorderSuggestionApiLinks() {
    }

    public static Link listSuggestions(String locationId, String sku) {
        return linkTo(methodOn(SellerReorderSuggestionController.class).listSuggestions(null, locationId, sku))
                .withRel("list-reorder-suggestions");
    }
}
