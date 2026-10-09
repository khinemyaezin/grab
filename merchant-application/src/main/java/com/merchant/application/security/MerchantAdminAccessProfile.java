package com.merchant.application.security;

import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RolePermissionReference;

import java.util.List;
import java.util.Map;

public final class MerchantAdminAccessProfile {
    public static final String ADMIN_ROLE_CODE = "MERCHANT_ADMIN";
    public static final String MERCHANT_SCOPE_KEY = MerchantScopeManifest.ACCOUNT_SCOPE_KEY;

    public static final RoleDeclaration DECLARATION = new RoleDeclaration(
            "merchant",
            ADMIN_ROLE_CODE,
            MERCHANT_SCOPE_KEY,
            1,
            Map.of("merchant", 1, "catalog", 1, "inventory", 1, "saleschannel", 1),
            List.of(
                    new RolePermissionReference("merchant", "MERCHANT_PROFILE_READ"),
                    new RolePermissionReference("merchant", "MERCHANT_PROFILE_WRITE"),
                    new RolePermissionReference("merchant", "MERCHANT_STOREFRONT_READ"),
                    new RolePermissionReference("merchant", "MERCHANT_STOREFRONT_WRITE"),
                    new RolePermissionReference("catalog", "CATALOG_READ"),
                    new RolePermissionReference("catalog", "CATALOG_WRITE"),
                    new RolePermissionReference("inventory", "INVENTORY_READ"),
                    new RolePermissionReference("inventory", "INVENTORY_WRITE"),
                    new RolePermissionReference("saleschannel", "SALES_CHANNEL_READ"),
                    new RolePermissionReference("saleschannel", "SALES_CHANNEL_WRITE")
            )
    );

    private MerchantAdminAccessProfile() {
    }

    public static String toRoleCode(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return null;
        }
        String trimmed = roleName.trim();
        if ("MERCHANT_ADMIN".equalsIgnoreCase(trimmed)) {
            return ADMIN_ROLE_CODE;
        }
        return trimmed;
    }
}
