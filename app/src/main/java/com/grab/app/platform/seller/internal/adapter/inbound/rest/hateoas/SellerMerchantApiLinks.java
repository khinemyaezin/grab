package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerMerchantAdminController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerMerchantController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerMerchantApiLinks {

    private SellerMerchantApiLinks() {
    }

    public static Link updateProfile(String merchantId) {
        return linkTo(methodOn(SellerMerchantController.class).updateProfile(merchantId, null, null))
                .withRel("update-merchant-profile");
    }

    public static Link getMerchant(String merchantId) {
        return linkTo(methodOn(SellerMerchantController.class).getMerchant(merchantId, null))
                .withRel("get-merchant");
    }

    public static Link listMyMerchants() {
        return linkTo(methodOn(SellerMerchantController.class).listMyMerchants(null))
                .withRel("list-merchants");
    }

    public static Link reviewQueue(String status) {
        return linkTo(methodOn(SellerMerchantAdminController.class).reviewQueue(status))
                .withRel("list-merchant-review-queue");
    }
}
