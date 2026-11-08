package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;

import java.util.Optional;
import java.util.Set;

public record ResolveExternalIdentityQuery(
        String issuer,
        String subject,
        Set<String> entitlements,
        AccessContext accessContext
) implements Query<Optional<AuthenticatedActor>> {
}

