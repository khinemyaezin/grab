package com.grab.framework.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SecurityManifestTest {
    @Test
    void digestIsIndependentOfDeclarationOrder() {
        var first = new SecurityManifest(
                "inventory", 4,
                List.of(new ScopeDeclaration("inventory.location", "merchant.account")),
                List.of(
                        new AuthorityDefinition("INVENTORY_READ", "read", "view"),
                        new AuthorityDefinition("INVENTORY_WRITE", "write", "manage")
                ),
                List.of("merchant.account", "catalog")
        );
        var reordered = new SecurityManifest(
                "inventory", 4,
                List.of(new ScopeDeclaration("inventory.location", "merchant.account")),
                List.of(
                        new AuthorityDefinition("INVENTORY_WRITE", "write", "manage"),
                        new AuthorityDefinition("INVENTORY_READ", "read", "view")
                ),
                List.of("catalog", "merchant.account")
        );

        assertEquals(first.contentDigest(), reordered.contentDigest());
    }

    @Test
    void digestChangesWhenRevisionOrDefinitionChanges() {
        var baseline = new SecurityManifest("catalog", 1, List.of(), List.of(
                new AuthorityDefinition("CATALOG_READ", "read", "view")
        ));
        var changed = new SecurityManifest("catalog", 2, List.of(), List.of(
                new AuthorityDefinition("CATALOG_READ", "read", "view")
        ));

        assertNotEquals(baseline.contentDigest(), changed.contentDigest());
    }

    @Test
    void digestIncludesLifecycleCategoryAndDependencyRevision() {
        var active = new SecurityManifest(
                1, "inventory", 3,
                List.of(new ScopeDeclaration("inventory.location", "merchant.account")),
                List.of(new AuthorityDefinition("inventory_read", "read", "view", "inventory", ScopeDeclaration.Lifecycle.ACTIVE)),
                List.of(new SecurityDependency("merchant.account", 2))
        );
        var retired = new SecurityManifest(
                1, "inventory", 3,
                List.of(new ScopeDeclaration("inventory.location", "merchant.account", ScopeDeclaration.Lifecycle.RETIRED)),
                List.of(new AuthorityDefinition("INVENTORY_READ", "read", "view", "inventory", ScopeDeclaration.Lifecycle.ACTIVE)),
                List.of(new SecurityDependency("merchant.account", 3))
        );

        assertNotEquals(active.contentDigest(), retired.contentDigest());
        assertEquals("INVENTORY_READ", active.authorities().getFirst().code());
    }
}
