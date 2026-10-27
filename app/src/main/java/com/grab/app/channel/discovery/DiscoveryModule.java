package com.grab.app.channel.discovery;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "shared",
        "catalog.catalog::query",
        "catalog.pricing::query",
        "catalog.inventory::query",
        "channel.saleschannel::query",
        "channel.region::query"
})
public class DiscoveryModule {
}
