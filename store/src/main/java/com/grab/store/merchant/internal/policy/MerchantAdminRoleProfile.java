package com.grab.store.merchant.internal.policy;

import java.util.Set;

/** Store-level policy for authorities granted to a newly provisioned merchant administrator. */
public final class MerchantAdminRoleProfile {
    public static final Set<String> AUTHORITIES = Set.of(
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

    private MerchantAdminRoleProfile() {
    }
}
