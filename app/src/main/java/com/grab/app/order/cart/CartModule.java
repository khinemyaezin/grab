package com.grab.app.order.cart;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "shared",
        "catalog.catalog::query",
        "catalog.pricing::query",
        "catalog.inventory::query",
        "channel.saleschannel::query"
})
public class CartModule {
}
