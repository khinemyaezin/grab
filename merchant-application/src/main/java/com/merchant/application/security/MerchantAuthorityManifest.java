package com.merchant.application.security;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifest;

import java.util.List;

public final class MerchantAuthorityManifest {
    public static final String GLOBAL_READ = "MERCHANT_GLOBAL_READ";
    public static final String LIFECYCLE_WRITE = "MERCHANT_LIFECYCLE_WRITE";
    public static final String APPLICATION_WRITE = "MERCHANT_APPLICATION_WRITE";
    public static final String PROFILE_WRITE = "MERCHANT_PROFILE_WRITE";
    public static final String PROFILE_READ = "MERCHANT_PROFILE_READ";
    public static final String STOREFRONT_READ = "MERCHANT_STOREFRONT_READ";
    public static final String STOREFRONT_WRITE = "MERCHANT_STOREFRONT_WRITE";

    public static final AuthorityManifest CURRENT = new AuthorityManifest("merchant", 1, List.of(
            new AuthorityDefinition(GLOBAL_READ, GLOBAL_READ, "Ability to view all merchants globally"),
            new AuthorityDefinition(LIFECYCLE_WRITE, LIFECYCLE_WRITE, "Ability to change merchant lifecycle status"),
            new AuthorityDefinition(APPLICATION_WRITE, APPLICATION_WRITE, "Ability to submit merchant applications"),
            new AuthorityDefinition(PROFILE_WRITE, PROFILE_WRITE, "Ability to update merchant profile details"),
            new AuthorityDefinition(PROFILE_READ, PROFILE_READ, "Ability to view own merchant profile"),
            new AuthorityDefinition(STOREFRONT_READ, STOREFRONT_READ, "Ability to view merchant storefronts"),
            new AuthorityDefinition(STOREFRONT_WRITE, STOREFRONT_WRITE, "Ability to manage merchant storefronts")
    ));

    private MerchantAuthorityManifest() {
    }
}
