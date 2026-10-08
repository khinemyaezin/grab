package com.inventory.application.security;

import com.grab.framework.security.SecurityDependency;
import com.grab.framework.security.SecurityManifest;

import java.util.List;

public final class InventorySecurityManifest {
    public static final SecurityManifest CURRENT = new SecurityManifest(
            "inventory", InventoryAuthorityManifest.CURRENT.version() + 1,
            InventoryScopeManifest.SCOPES, InventoryAuthorityManifest.CURRENT.definitions(),
            List.of(new SecurityDependency("merchant.account", 2))
    );

    private InventorySecurityManifest() {
    }
}
