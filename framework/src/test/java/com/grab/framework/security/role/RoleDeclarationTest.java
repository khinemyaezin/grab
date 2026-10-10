package com.grab.framework.security.role;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleDeclarationTest {

    @Test
    void contentDigestIsIndependentOfMapAndPermissionOrder() {
        RoleDeclaration first = new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1,
                Map.of("catalog", 1, "inventory", 2),
                List.of(new RolePermissionReference("catalog", "CATALOG_READ"),
                        new RolePermissionReference("inventory", "INVENTORY_READ")));
        RoleDeclaration second = new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1,
                Map.of("inventory", 2, "catalog", 1),
                List.of(new RolePermissionReference("inventory", "INVENTORY_READ"),
                        new RolePermissionReference("catalog", "CATALOG_READ")));

        assertEquals(first.contentDigest(), second.contentDigest());
    }

    @Test
    void contentDigestSeparatesModuleKeysFromRevisions() {
        RoleDeclaration first = new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1, Map.of("x", 12),
                List.of(new RolePermissionReference("merchant", "MERCHANT_PROFILE_READ")));
        RoleDeclaration second = new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1, Map.of("x1", 2),
                List.of(new RolePermissionReference("merchant", "MERCHANT_PROFILE_READ")));

        assertEquals(first.legacyContentDigest(), second.legacyContentDigest());
        assertNotEquals(first.contentDigest(), second.contentDigest());
    }

    @Test
    void duplicatePermissionReferencesAreRejected() {
        RolePermissionReference permission = new RolePermissionReference("catalog", "CATALOG_READ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1,
                Map.of("catalog", 1), List.of(permission, permission)));

        assertTrue(exception.getMessage().contains("duplicate references"));
    }
}
