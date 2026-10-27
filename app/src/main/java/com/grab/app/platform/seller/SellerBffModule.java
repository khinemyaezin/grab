package com.grab.app.platform.seller;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "shared",
        "workflows",
        "catalog.catalog",
        "catalog.inventory",
        "catalog.pricing",
        "account.identity",
        "account.merchant",
        "account.customer",
        "channel.saleschannel",
        "channel.region",
        "channel.discovery",
        "order.cart"
})
public class SellerBffModule {
}
