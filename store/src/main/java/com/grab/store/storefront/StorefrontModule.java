package com.grab.store.storefront;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "shared",
        "catalog::queries",
        "catalog::api",
        "pricing::queries",
        "inventory::queries"
})
public class StorefrontModule {
}
