package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerMerchantApplicationController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerMerchantApplicationApiLinks {

    private SellerMerchantApplicationApiLinks() {
    }

    public static Link getC2CApplication() {
        return linkTo(methodOn(SellerMerchantApplicationController.class).getC2CApplication(null))
                .withRel("get-c2c-application");
    }

    public static Link submitApplication(String applicationId) {
        return linkTo(methodOn(SellerMerchantApplicationController.class).submitApplication(applicationId, null))
                .withRel("submit-merchant-application");
    }

    public static Link getFirstPartyApplication() {
        return linkTo(methodOn(SellerMerchantApplicationController.class).getFirstPartyApplication(null))
                .withRel("get-first-party-retailer-application");
    }

    public static Link startApplication() {
        return linkTo(methodOn(SellerMerchantApplicationController.class).startApplication(null, null))
                .withRel("create-merchant-application");
    }

    public static Link startC2CApplication() {
        return linkTo(methodOn(SellerMerchantApplicationController.class).startC2CApplication(null, null))
                .withRel("create-c2c-application");
    }

    public static Link startFirstPartyApplication() {
        return linkTo(methodOn(SellerMerchantApplicationController.class).startFirstPartyApplication(null, null))
                .withRel("create-first-party-retailer-application");
    }
}
