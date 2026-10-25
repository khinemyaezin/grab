package com.grab.app.channeldiscovery.api;

import org.springframework.hateoas.Link;
import com.grab.app.channeldiscovery.saleschannel.SalesChannelRootController;
import com.grab.app.channeldiscovery.storefrontquery.StorefrontQueryRootController;
import com.grab.app.channeldiscovery.region.RegionRootController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class ChannelDiscoveryApiLinks {
    private ChannelDiscoveryApiLinks() {}

    public static Link salesChannelRoot() {
        return linkTo(methodOn(SalesChannelRootController.class).root()).withRel("get-sales-channel-root");
    }
    public static Link storefrontQueryRoot() {
        return linkTo(methodOn(StorefrontQueryRootController.class).root()).withRel("get-storefront-query-root");
    }
    public static Link regionRoot() {
        return linkTo(methodOn(RegionRootController.class).root()).withRel("get-region-root");
    }
}
