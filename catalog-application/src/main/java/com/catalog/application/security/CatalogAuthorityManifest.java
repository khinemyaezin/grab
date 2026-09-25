package com.catalog.application.security;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifest;

import java.util.List;

public final class CatalogAuthorityManifest {
    public static final String READ = "CATALOG_READ";
    public static final String WRITE = "CATALOG_WRITE";

    public static final AuthorityManifest CURRENT = new AuthorityManifest("catalog", 1, List.of(
            new AuthorityDefinition(READ, READ, "Ability to view catalog items"),
            new AuthorityDefinition(WRITE, WRITE, "Ability to modify catalog items")
    ));

    private CatalogAuthorityManifest() {
    }
}
