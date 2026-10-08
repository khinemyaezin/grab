package com.identity.application.security;

import com.grab.framework.security.SecurityManifest;

import java.util.List;

public final class IdentitySecurityManifest {
    public static final SecurityManifest CURRENT = new SecurityManifest(
            "identity", IdentityAuthorityManifest.CURRENT.version() + 1,
            List.of(), IdentityAuthorityManifest.CURRENT.definitions()
    );

    private IdentitySecurityManifest() {
    }
}
