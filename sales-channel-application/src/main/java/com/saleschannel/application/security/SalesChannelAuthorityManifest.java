package com.saleschannel.application.security;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifest;

import java.util.List;

public final class SalesChannelAuthorityManifest {
    public static final String READ = "SALES_CHANNEL_READ";
    public static final String WRITE = "SALES_CHANNEL_WRITE";

    public static final AuthorityManifest CURRENT = new AuthorityManifest("saleschannel", 1, List.of(
            new AuthorityDefinition(READ, READ, "Ability to view sales channels"),
            new AuthorityDefinition(WRITE, WRITE, "Ability to enable or disable seller-owned sales channels")
    ));

    private SalesChannelAuthorityManifest() {
    }
}
