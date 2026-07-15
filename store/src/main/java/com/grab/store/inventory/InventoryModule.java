package com.grab.store.inventory;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "catalog::events"})
public class InventoryModule {
}
