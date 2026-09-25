package com.identity.application.security;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifest;

import java.util.List;

public final class IdentityAuthorityManifest {
    public static final String USER_READ = "USER_READ";
    public static final String USER_WRITE = "USER_WRITE";
    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_WRITE = "ROLE_WRITE";
    public static final String ACCESS_ASSIGNMENT_READ = "ACCESS_ASSIGNMENT_READ";
    public static final String ACCESS_ASSIGNMENT_WRITE = "ACCESS_ASSIGNMENT_WRITE";
    public static final String ACCESS_INVITATION_WRITE = "ACCESS_INVITATION_WRITE";

    public static final AuthorityManifest CURRENT = new AuthorityManifest("identity", 1, List.of(
            new AuthorityDefinition(USER_READ, USER_READ, "Ability to read user details"),
            new AuthorityDefinition(USER_WRITE, USER_WRITE, "Ability to manage and modify users"),
            new AuthorityDefinition(ROLE_READ, ROLE_READ, "Ability to read roles"),
            new AuthorityDefinition(ROLE_WRITE, ROLE_WRITE, "Ability to manage and modify roles"),
            new AuthorityDefinition(ACCESS_ASSIGNMENT_READ, ACCESS_ASSIGNMENT_READ, "Ability to read scoped access assignments"),
            new AuthorityDefinition(ACCESS_ASSIGNMENT_WRITE, ACCESS_ASSIGNMENT_WRITE, "Ability to manage scoped access assignments"),
            new AuthorityDefinition(ACCESS_INVITATION_WRITE, ACCESS_INVITATION_WRITE, "Ability to manage scoped staff invitations")
    ));

    private IdentityAuthorityManifest() {
    }
}
