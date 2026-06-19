package com.grab.framework.security;

public interface PlatformIdentityResolver {
    AuthenticatedActor resolve(ExternalPrincipal principal);
}
