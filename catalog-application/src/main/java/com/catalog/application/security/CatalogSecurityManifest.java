package com.catalog.application.security;

import com.grab.framework.security.SecurityManifest;

import java.util.List;

public final class CatalogSecurityManifest {
    public static final int SECURITY_REVISION = 2;
    public static final SecurityManifest CURRENT = new SecurityManifest(
            "catalog", SECURITY_REVISION,
            List.of(), CatalogAuthorityManifest.CURRENT.definitions()
    );

    private CatalogSecurityManifest() {
    }
}
