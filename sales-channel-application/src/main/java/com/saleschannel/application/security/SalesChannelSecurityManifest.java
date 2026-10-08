package com.saleschannel.application.security;

import com.grab.framework.security.SecurityManifest;

import java.util.List;

public final class SalesChannelSecurityManifest {
    public static final SecurityManifest CURRENT = new SecurityManifest(
            "saleschannel", SalesChannelAuthorityManifest.CURRENT.version() + 1,
            List.of(), SalesChannelAuthorityManifest.CURRENT.definitions()
    );

    private SalesChannelSecurityManifest() {
    }
}
