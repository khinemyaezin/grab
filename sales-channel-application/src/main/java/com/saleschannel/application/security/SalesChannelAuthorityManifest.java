package com.saleschannel.application.security;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifest;

import java.util.List;

public final class SalesChannelAuthorityManifest {
    public static final String READ = "SALES_CHANNEL_READ";
    public static final String WRITE = "SALES_CHANNEL_WRITE";
    public static final String KEY_READ = "SALES_CHANNEL_KEY_READ";
    public static final String KEY_WRITE = "SALES_CHANNEL_KEY_WRITE";
    public static final String KEY_ADMIN = "SALES_CHANNEL_KEY_ADMIN";

    public static final AuthorityManifest CURRENT = new AuthorityManifest("saleschannel", 2, List.of(
            new AuthorityDefinition(READ, READ, "Ability to view sales channels"),
            new AuthorityDefinition(WRITE, WRITE, "Ability to enable or disable seller-owned sales channels"),
            new AuthorityDefinition(KEY_READ, KEY_READ, "Ability to view publishable keys for owned channels"),
            new AuthorityDefinition(KEY_WRITE, KEY_WRITE, "Ability to create and revoke publishable keys for owned channels"),
            new AuthorityDefinition(KEY_ADMIN, KEY_ADMIN, "Ability to manage publishable keys across all channels")
    ));

    private SalesChannelAuthorityManifest() {
    }
}
