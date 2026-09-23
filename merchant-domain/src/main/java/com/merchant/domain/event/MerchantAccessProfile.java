package com.merchant.domain.event;

import com.merchant.domain.valueobject.MerchantRole;

public final class MerchantAccessProfile {
    public static final String SELLER_PLATFORM_CODE = "SELLER_PORTAL";
    public static final String ADMIN_ROLE_CODE = "MERCHANT_ADMIN";
    public static final String MERCHANT_SCOPE_KEY = "merchant.account";

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
