package com.identity.adapter.persistence.adapter;

import com.grab.framework.security.AccessContext;
import com.identity.domain.valueobject.SessionDetails;
import com.identity.domain.port.outbound.SessionStore;
import com.identity.adapter.persistence.entity.RefreshSessionEntity;
import com.identity.adapter.persistence.entity.UserEntity;
import com.identity.adapter.persistence.repository.jpa.RefreshSessionJpaRepository;
import com.identity.adapter.persistence.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class SessionStoreAdapter implements SessionStore {

    private final RefreshSessionJpaRepository sessionRepository;
    private final UserJpaRepository userRepository;

    @Override
    public void saveNewSession(
            String userId,
            String tokenHash,
            String tokenFamilyId,
            Instant expiresAt,
            AccessContext accessContext
    ) {
        UserEntity user = userRepository.findByUuid(userId).orElseThrow();
        RefreshSessionEntity session = new RefreshSessionEntity();
        session.setUser(user);
        session.setTokenHash(tokenHash);
        session.setTokenFamilyId(tokenFamilyId);
        session.setCreatedAt(Instant.now());
        session.setExpiresAt(expiresAt);
        if(Objects.nonNull(accessContext)) {
            session.setPlatformCode(accessContext.platformCode());
            session.setAssignmentUuid(accessContext.assignmentId());
            session.setScopeKey(accessContext.scopeKey());
            session.setScopeId(accessContext.scopeId());
        }
        sessionRepository.save(session);
    }

    @Override
    public Optional<SessionDetails> findByTokenHash(String tokenHash) {
        return sessionRepository.findByTokenHash(tokenHash).map(entity -> 
            new SessionDetails(
                entity.getUser().getUuid(),
                entity.getUser().getEmail(),
                entity.getTokenFamilyId(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                contextOf(entity).orElse(null)
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

    @Override
    public void revokeByAssignment(String assignmentId) {
        Instant now = Instant.now();
        var assignmentSessions = sessionRepository.findByAssignmentUuid(assignmentId);
        assignmentSessions.forEach(session -> {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(now);
            }
        });
        sessionRepository.saveAll(assignmentSessions);
    }

    @Override
    public void revokeByScope(String platformCode, String scopeKey, String scopeId) {
        Instant now = Instant.now();
        var scopedSessions = sessionRepository.findByPlatformCodeAndScopeKeyAndScopeIdAndRevokedAtIsNull(
                platformCode, scopeKey, scopeId);
        scopedSessions.forEach(session -> session.setRevokedAt(now));
        sessionRepository.saveAll(scopedSessions);
    }

    private Optional<AccessContext> contextOf(RefreshSessionEntity session) {
        if (session.getPlatformCode() == null
                || session.getAssignmentUuid() == null
                || session.getScopeKey() == null
                || session.getScopeId() == null) {
            return Optional.empty();
        }
        return Optional.of(new AccessContext(
                session.getPlatformCode(),
                session.getAssignmentUuid(),
                session.getScopeKey(),
                session.getScopeId()
        ));
    }
}
