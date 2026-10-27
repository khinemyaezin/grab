package com.grab.app.catalog.catalog;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "workflows::events", "workflows::api", "account.merchant::events"})
public class CatalogModule {
}
