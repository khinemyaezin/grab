package com.merchant.application.security;

import com.grab.framework.security.SecurityManifest;

public final class MerchantSecurityManifest {
    public static final int SECURITY_REVISION = 2;
    public static final SecurityManifest CURRENT = new SecurityManifest(
            "merchant", SECURITY_REVISION,
            MerchantScopeManifest.SCOPES, MerchantAuthorityManifest.CURRENT.definitions()
    );

    private MerchantSecurityManifest() {
    }
}
