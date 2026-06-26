package com.identity.infrastructure.repository.adapter;

import com.identity.domain.valueobject.RefreshSessionDetails;
import com.identity.domain.repository.RefreshSessionStore;
import com.identity.infrastructure.entity.RefreshSessionEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.RefreshSessionJpaRepository;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class JpaRefreshSessionStoreAdapter implements RefreshSessionStore {

    private final RefreshSessionJpaRepository sessionRepository;
    private final UserJpaRepository userRepository;

    @Override
    public void saveNewSession(String userId, String tokenHash, String tokenFamilyId, Instant expiresAt) {
        UserEntity user = userRepository.findByUuid(userId).orElseThrow();
        RefreshSessionEntity session = new RefreshSessionEntity();
        session.setUser(user);
        session.setTokenHash(tokenHash);
        session.setTokenFamilyId(tokenFamilyId);
        session.setCreatedAt(Instant.now());
        session.setExpiresAt(expiresAt);
        sessionRepository.save(session);
    }

    @Override
    public Optional<RefreshSessionDetails> findByTokenHash(String tokenHash) {
        return sessionRepository.findByTokenHash(tokenHash).map(entity -> 
            new RefreshSessionDetails(
                entity.getUser().getUuid(),
                entity.getUser().getEmail(),
                entity.getTokenFamilyId(),
                entity.getExpiresAt(),
                entity.getRevokedAt()
            )
        );
    }

    @Override
    public void revokeFamily(String tokenFamilyId) {
        Instant now = Instant.now();
        var family = sessionRepository.findByTokenFamilyId(tokenFamilyId);
        family.forEach(member -> {
            if (member.getRevokedAt() == null) {
                member.setRevokedAt(now);
            }
        });
        sessionRepository.saveAll(family);
    }

    @Override
    public void replaceSession(String oldTokenHash, String newTokenHash, Instant oldRevokedAt) {
        RefreshSessionEntity oldSession = sessionRepository.findByTokenHash(oldTokenHash).orElseThrow();
        RefreshSessionEntity newSession = sessionRepository.findByTokenHash(newTokenHash).orElseThrow();
        
        oldSession.setRevokedAt(oldRevokedAt);
        oldSession.setLastUsedAt(oldRevokedAt);
        oldSession.setReplacedById(newSession.getId());
        sessionRepository.save(oldSession);
    }

    @Override
    public void revokeSession(String tokenHash) {
        sessionRepository.findByTokenHash(tokenHash).ifPresent(s -> {
            if (s.getRevokedAt() == null) {
                s.setRevokedAt(Instant.now());
                sessionRepository.save(s);
            }
        });
    }

    @Override
    public void revokeAll(String userId) {
        Instant now = Instant.now();
        var userSessions = sessionRepository.findByUser_Uuid(userId);
        userSessions.forEach(session -> {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(now);
            }
        });
        sessionRepository.saveAll(userSessions);
    }
}
