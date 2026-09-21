package com.grab.store.storefrontquery;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "shared",
        "catalog::events",
        "pricing::events",
        "inventory::events",
        "saleschannel::events",
        "catalog::query",
        "pricing::query",
        "inventory::query",
        "saleschannel::query"
})
public class StorefrontQueryModule {
}
