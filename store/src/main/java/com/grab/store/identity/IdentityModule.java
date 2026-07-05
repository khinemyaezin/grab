package com.grab.store.identity;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "merchant::events"})
public class IdentityModule {}
