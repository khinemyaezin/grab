package com.identity.application.security;

import com.grab.framework.security.SecurityManifest;

import java.util.List;

public final class IdentitySecurityManifest {
    public static final int SECURITY_REVISION = 2;
    public static final SecurityManifest CURRENT = new SecurityManifest(
            "identity", SECURITY_REVISION,
            List.of(), IdentityAuthorityManifest.CURRENT.definitions()
    );

    private IdentitySecurityManifest() {
    }
}
