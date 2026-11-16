package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerPriceListController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerPricePreferenceController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerPriceSetController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerPricingApiLinks {

    private SellerPricingApiLinks() {
    }

    public static Link getPreference(String preferenceId) {
        return linkTo(methodOn(SellerPricePreferenceController.class).getPreference(preferenceId))
                .withRel("get-price-preference");
    }

    public static Link getPriceSet(String priceSetId) {
        return linkTo(methodOn(SellerPriceSetController.class).getPriceSet(priceSetId))
                .withRel("get-price-set");
    }

    public static Link calculatePrices() {
        return linkTo(methodOn(SellerPriceSetController.class).calculatePrices(null))
                .withRel("calculate-prices");
    }

    public static Link getPriceList(String priceListId) {
        return linkTo(methodOn(SellerPriceListController.class).getPriceList(priceListId))
                .withRel("get-price-list");
    }

    public static Link listPriceLists() {
        return linkTo(methodOn(SellerPriceListController.class).listPriceLists())
                .withRel("list-price-lists");
    }

    public static Link createPriceList() {
        return linkTo(methodOn(SellerPriceListController.class).createPriceList(null))
                .withRel("create-price-list");
    }

    public static Link createPriceSet() {
        return linkTo(methodOn(SellerPriceSetController.class).createPriceSet())
                .withRel("create-price-set");
    }

    public static Link createPreference() {
        return linkTo(methodOn(SellerPricePreferenceController.class).createPreference(null))
                .withRel("create-price-preference");
    }
}
