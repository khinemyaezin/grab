package com.merchant.domain.event;

import com.merchant.domain.valueobject.MerchantRole;

import java.util.Set;

public final class MerchantAccessProfile {
    public static final String ADMIN_ROLE_CODE = "MERCHANT_ADMIN";
    public static final String MERCHANT_SCOPE_KEY = "merchant.account";

    public static final Set<String> DEFAULT_ADMIN_AUTHORITIES = Set.of(
            "MERCHANT_PROFILE_READ",
            "MERCHANT_PROFILE_WRITE",
            "MERCHANT_STOREFRONT_READ",
            "MERCHANT_STOREFRONT_WRITE",
            "ACCESS_ASSIGNMENT_READ",
            "ACCESS_ASSIGNMENT_WRITE",
            "ACCESS_INVITATION_WRITE",
            "ROLE_READ",
            "ROLE_WRITE",
            "INVENTORY_READ",
            "INVENTORY_WRITE",
            "SALES_CHANNEL_READ",
            "SALES_CHANNEL_WRITE",
            "CATALOG_READ",
            "CATALOG_WRITE"
    );

    private MerchantAccessProfile() {
    }

    public static String toRoleCode(MerchantRole role) {
        if (role == null) {
            return null;
        }
        return role.isAdmin() ? ADMIN_ROLE_CODE : role.name();
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
