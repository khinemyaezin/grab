package com.grab.store.identity;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "merchant::events", "customer::events"})
public class IdentityModule {}
