package com.grab.store.storefrontquery;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "shared",
        "catalog::query",
        "pricing::query",
        "inventory::query",
        "saleschannel::query",
        "region::query"
})
public class StorefrontQueryModule {
}
