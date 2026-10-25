package com.grab.app.identityaccount.api;

import org.springframework.hateoas.Link;
import com.grab.app.identityaccount.identity.IdentityRootController;
import com.grab.app.identityaccount.merchant.MerchantRootController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class IdentityAccountApiLinks {
    private IdentityAccountApiLinks() {}

    public static Link identityRoot() {
        return linkTo(methodOn(IdentityRootController.class).root()).withRel("get-identity-root");
    }
    public static Link merchantRoot() {
        return linkTo(methodOn(MerchantRootController.class).root()).withRel("get-merchant-root");
    }
}
