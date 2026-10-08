package com.merchant.application.security;

import com.grab.framework.security.SecurityManifest;

public final class MerchantSecurityManifest {
    public static final SecurityManifest CURRENT = new SecurityManifest(
            "merchant", MerchantAuthorityManifest.CURRENT.version() + 1,
            MerchantScopeManifest.SCOPES, MerchantAuthorityManifest.CURRENT.definitions()
    );

    private MerchantSecurityManifest() {
    }
}
