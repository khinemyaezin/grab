package com.grab.store.catalog;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "workflows::events"})
public class CatalogModule {
}
