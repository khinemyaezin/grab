package com.identity.domain.service;

import com.grab.framework.id.Id;
import com.grab.framework.security.AuthenticatedActor;

public interface TokenLifeCycle {
    TokenPair issue(AuthenticatedActor actor);
    TokenPair refresh(String refreshToken);
    void revoke(String refreshToken);
    void revokeAll(Id userId);
}
