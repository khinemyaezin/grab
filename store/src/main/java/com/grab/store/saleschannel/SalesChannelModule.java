package com.grab.store.saleschannel;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "workflows::events", "region::query"})
public class SalesChannelModule {
}
