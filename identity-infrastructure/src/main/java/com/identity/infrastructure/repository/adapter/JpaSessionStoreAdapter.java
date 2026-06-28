package com.identity.infrastructure.repository.adapter;

import com.grab.framework.security.AccessContext;
import com.identity.domain.valueobject.SessionDetails;
import com.identity.domain.repository.SessionStore;
import com.identity.infrastructure.entity.RefreshSessionEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.RefreshSessionJpaRepository;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class JpaSessionStoreAdapter implements SessionStore {

    private final RefreshSessionJpaRepository sessionRepository;
    private final UserJpaRepository userRepository;

    @Override
    public void saveNewSession(
            String userId,
            String tokenHash,
            String tokenFamilyId,
            Instant expiresAt,
            Optional<AccessContext> accessContext
    ) {
        UserEntity user = userRepository.findByUuid(userId).orElseThrow();
        RefreshSessionEntity session = new RefreshSessionEntity();
        session.setUser(user);
        session.setTokenHash(tokenHash);
        session.setTokenFamilyId(tokenFamilyId);
        session.setCreatedAt(Instant.now());
        session.setExpiresAt(expiresAt);
        accessContext.ifPresent(context -> {
            session.setPlatformCode(context.platformCode());
            session.setAssignmentUuid(context.assignmentId());
            session.setScopeType(context.scopeType());
            session.setScopeId(context.scopeId());
        });
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
                contextOf(entity)
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

    private Optional<AccessContext> contextOf(RefreshSessionEntity session) {
        if (session.getPlatformCode() == null
                || session.getAssignmentUuid() == null
                || session.getScopeType() == null
                || session.getScopeId() == null) {
            return Optional.empty();
        }
        return Optional.of(new AccessContext(
                session.getPlatformCode(),
                session.getAssignmentUuid(),
                session.getScopeType(),
                session.getScopeId()
        ));
    }
}
