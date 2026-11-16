package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerDiscoveryApiLinks {

    private SellerDiscoveryApiLinks() {
    }

    public static Link regionRoot() {
        return linkTo(methodOn(SellerRegionRootController.class).root())
                .withRel("get-region-root");
    }

    public static Link authRoot() {
        return linkTo(methodOn(SellerAuthRootController.class).root())
                .withRel("get-identity-root");
    }

    public static Link salesChannelRoot() {
        return linkTo(methodOn(SellerSalesChannelRootController.class).root())
                .withRel("get-sales-channel-root");
    }

    public static Link merchantRoot() {
        return linkTo(methodOn(SellerMerchantRootController.class).root())
                .withRel("get-merchant-root");
    }

    public static Link catalogRoot() {
        return linkTo(methodOn(SellerCatalogRootController.class).root())
                .withRel("get-catalog-root");
    }

    public static Link pricingRoot() {
        return linkTo(methodOn(SellerPricingRootController.class).root())
                .withRel("get-pricing-root");
    }

    public static Link inventoryRoot() {
        return linkTo(methodOn(SellerInventoryRootController.class).root())
                .withRel("get-inventory-root");
    }

    public static Link apiRoot() {
        return linkTo(methodOn(SellerApiRootController.class).root())
                .withSelfRel();
    }

    public static Link workflowRoot() {
        return linkTo(methodOn(SellerWorkflowRootController.class).root())
                .withRel("get-workflows-root");
    }
}
