package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerPublishableKeyController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerPublishableKeyApiLinks {

    private SellerPublishableKeyApiLinks() {
    }

    public static Link createPublishableKey(String channelId) {
        return linkTo(methodOn(SellerPublishableKeyController.class).createPublishableKey(channelId, null))
                .withRel("create-publishable-key");
    }

    public static Link revokePublishableKey(String channelId, String keyId) {
        return linkTo(methodOn(SellerPublishableKeyController.class).revokePublishableKey(channelId, keyId))
                .withRel("revoke-publishable-key");
    }

    public static Link resolveKey(String token) {
        return linkTo(methodOn(SellerPublishableKeyController.class).resolveKey(token))
                .withRel("resolve-publishable-key");
    }
}
