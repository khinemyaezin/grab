package com.identity.infrastructure.security;

import com.grab.framework.id.Id;
import com.identity.domain.service.RefreshSessionRevoker;
import com.identity.infrastructure.repository.jpa.RefreshSessionJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class JpaRefreshSessionRevoker implements RefreshSessionRevoker {

    private final RefreshSessionJpaRepository sessions;

    @Override
    public void revokeAll(Id userId) {
        Instant revokedAt = Instant.now();
        sessions.findByUser_Uuid(userId.getValue()).forEach(session -> session.setRevokedAt(revokedAt));
    }
}
