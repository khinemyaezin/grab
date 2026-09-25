package com.inventory.application.security;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifest;

import java.util.List;

public final class InventoryAuthorityManifest {
    public static final String READ = "INVENTORY_READ";
    public static final String WRITE = "INVENTORY_WRITE";

    public static final AuthorityManifest CURRENT = new AuthorityManifest("inventory", 1, List.of(
            new AuthorityDefinition(READ, READ, "Ability to view inventory locations, zones, bins, and items"),
            new AuthorityDefinition(WRITE, WRITE, "Ability to manage inventory locations, zones, bins, and items")
    ));

    private InventoryAuthorityManifest() {
    }
}
