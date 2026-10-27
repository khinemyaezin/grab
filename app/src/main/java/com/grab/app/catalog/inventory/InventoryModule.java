package com.grab.app.catalog.inventory;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "catalog.catalog::api", "workflows::events"})
public class InventoryModule {
}
