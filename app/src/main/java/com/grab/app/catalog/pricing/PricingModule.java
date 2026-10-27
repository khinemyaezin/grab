package com.grab.app.catalog.pricing;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "workflows::events"})
public class PricingModule {
}
