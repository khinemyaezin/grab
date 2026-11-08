package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;

import java.util.Optional;

public record ResolvePlatformUserQuery(
        String issuer,
        String userId,
        AccessContext accessContext
) implements Query<Optional<AuthenticatedActor>> {
}

