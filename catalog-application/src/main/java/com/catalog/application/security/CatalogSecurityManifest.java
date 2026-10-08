package com.catalog.application.security;

import com.grab.framework.security.SecurityManifest;

import java.util.List;

public final class CatalogSecurityManifest {
    public static final SecurityManifest CURRENT = new SecurityManifest(
            "catalog", CatalogAuthorityManifest.CURRENT.version() + 1,
            List.of(), CatalogAuthorityManifest.CURRENT.definitions()
    );

    private CatalogSecurityManifest() {
    }
}
