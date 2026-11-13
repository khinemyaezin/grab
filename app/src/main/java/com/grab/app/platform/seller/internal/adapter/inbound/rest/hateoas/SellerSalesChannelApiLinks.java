package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerSalesChannelController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerSalesChannelApiLinks {

    private SellerSalesChannelApiLinks() {
    }

    public static Link getChannel(String salesChannelId) {
        return linkTo(methodOn(SellerSalesChannelController.class).getChannel(salesChannelId, null))
                .withRel("get-sales-channel");
    }

    public static Link listChannels(int page, int size) {
        return linkTo(methodOn(SellerSalesChannelController.class).listChannels(null, page, size))
                .withRel("list-sales-channels");
    }

    public static Link provisionMerchantWebsiteChannel() {
        return linkTo(methodOn(SellerSalesChannelController.class).provisionMerchantWebsiteChannel(null, null))
                .withRel("create-merchant-website-channel");
    }
}
