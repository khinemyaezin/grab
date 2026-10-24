package com.grab.store.pricing;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "workflows::events"})
public class PricingModule {
}
