package com.grab.app.catalog.pricing.api;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.PriceSetController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.VariantPriceSetLinkController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class PricingApiLinks {

    private PricingApiLinks() {
    }

    public static Link listVariantPriceLinks() {
        return linkTo(methodOn(VariantPriceSetLinkController.class).list(null))
                .withRel("list-variant-price-links");
    }

    public static Link calculatePrices() {
        return linkTo(methodOn(PriceSetController.class).calculatePrices(null))
                .withRel("calculate-prices");
    }
}
