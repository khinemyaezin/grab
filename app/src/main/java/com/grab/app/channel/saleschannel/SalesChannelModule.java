package com.grab.app.channel.saleschannel;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "workflows::events", "channel.region::query"})
public class SalesChannelModule {
}
