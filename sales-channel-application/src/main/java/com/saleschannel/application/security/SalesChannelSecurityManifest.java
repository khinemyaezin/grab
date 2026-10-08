package com.saleschannel.application.security;

import com.grab.framework.security.SecurityManifest;

import java.util.List;

public final class SalesChannelSecurityManifest {
    public static final int SECURITY_REVISION = 2;
    public static final SecurityManifest CURRENT = new SecurityManifest(
            "saleschannel", SECURITY_REVISION,
            List.of(), SalesChannelAuthorityManifest.CURRENT.definitions()
    );

    private SalesChannelSecurityManifest() {
    }
}
