package com.identity.domain.service;

import com.grab.framework.security.AuthenticatedActor;

public interface TokenIssuer {
    TokenPair issue(AuthenticatedActor actor);
    TokenPair refresh(String refreshToken);
    void revoke(String refreshToken);
}
