package com.inventory.application.security;

import com.grab.framework.security.ScopeDeclaration;

import java.util.List;
import java.util.Set;

public final class InventoryScopeManifest {
    public static final int VERSION = 1;
    public static final String FULFILLMENT_LOCATION_SCOPE_KEY = "inventory.fulfillment-location";

    public static final List<ScopeDeclaration> SCOPES = List.of(
            new ScopeDeclaration(FULFILLMENT_LOCATION_SCOPE_KEY, "merchant.account")
    );

    public static final Set<String> SUPPORTED_SCOPE_KEYS = Set.of(
            "merchant.account",
            FULFILLMENT_LOCATION_SCOPE_KEY,
            "merchant.storefront"
    );

    private InventoryScopeManifest() {
    }

    public static boolean supports(String scopeKey) {
        return SUPPORTED_SCOPE_KEYS.contains(scopeKey);
    }
}
