package com.grab.store.cart;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "shared",
        "catalog::query",
        "pricing::query",
        "inventory::query",
        "saleschannel::query"
})
public class CartModule {
}
