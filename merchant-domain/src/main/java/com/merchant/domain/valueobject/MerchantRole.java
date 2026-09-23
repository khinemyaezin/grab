package com.merchant.domain.valueobject;

import java.util.Objects;
import java.util.Set;

public record MerchantRole(
        String name,
        Set<String> authorities
) {
    public static final String MERCHANT_ADMIN = "MERCHANT_ADMIN";
    public static final String WILDCARD_AUTHORITY = "*";

    public MerchantRole {
        Objects.requireNonNull(name, "role name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("role name cannot be blank");
        }
        authorities = authorities != null ? Set.copyOf(authorities) : Set.of();
    }

    public static MerchantRole merchantAdmin() {
        return new MerchantRole(MERCHANT_ADMIN, Set.of(WILDCARD_AUTHORITY));
    }

    public static MerchantRole of(String name) {
        return new MerchantRole(name, Set.of());
    }

    public static MerchantRole of(String name, Set<String> authorities) {
        return new MerchantRole(name, authorities);
    }

    public boolean isAdmin() {
        return MERCHANT_ADMIN.equalsIgnoreCase(name);
    }

    public boolean hasAuthority(String authorityCode) {
        if (isAdmin() || authorities.contains(WILDCARD_AUTHORITY)) {
            return true;
        }
        return authorityCode != null && authorities.contains(authorityCode);
    }

    public boolean canManage(MerchantRole targetRole) {
        if (targetRole == null) {
            return false;
        }
        return this.isAdmin();
    }
}
