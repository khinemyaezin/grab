package com.grab.framework.security;

public interface AccessTokenAuthenticator {
    ExternalPrincipal authenticate(String bearerToken);
}
