package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerStorefrontController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerStorefrontApiLinks {

    private SellerStorefrontApiLinks() {
    }

    public static Link getStorefront(String merchantId, String storefrontId) {
        return linkTo(methodOn(SellerStorefrontController.class).getStorefront(merchantId, storefrontId))
                .withRel("get-storefront");
    }

    public static Link listStorefronts(String merchantId) {
        return linkTo(methodOn(SellerStorefrontController.class).listStorefronts(merchantId))
                .withRel("list-storefronts");
    }

    public static Link createStorefront(String merchantId) {
        return linkTo(methodOn(SellerStorefrontController.class).createStorefront(merchantId, null))
                .withRel("create-storefront");
    }

    public static Link updateStorefront(String merchantId, String storefrontId) {
        return linkTo(methodOn(SellerStorefrontController.class).updateStorefront(merchantId, storefrontId, null))
                .withRel("update-storefront");
    }

    public static Link closeStorefront(String merchantId, String storefrontId) {
        return linkTo(methodOn(SellerStorefrontController.class).closeStorefront(merchantId, storefrontId))
                .withRel("close-storefront");
    }

    public static Link activateStorefront(String merchantId, String storefrontId) {
        return linkTo(methodOn(SellerStorefrontController.class).activateStorefront(merchantId, storefrontId))
                .withRel("activate-storefront");
    }
}
