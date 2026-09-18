package com.grab.store.saleschannel;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "workflows::events"})
public class SalesChannelModule {
}
