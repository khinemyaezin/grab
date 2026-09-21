package com.identity.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.identity.adapter.persistence.entity.AccessAssignmentEntity;
import com.identity.adapter.persistence.repository.jpa.AccessAssignmentJpaRepository;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.model.read.AccessAssignmentView;
import com.identity.domain.enums.AccessAssignmentStatus;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class AccessAssignmentQueryAdapter implements AccessAssignmentQueryPort {
    private final AccessAssignmentJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public List<AccessAssignmentView> findByUser(String userId) {
        return executor.query("AccessAssignment", () ->
                jpaRepository.findByUser_UuidOrderByCreatedAt(userId).stream()
                        .map(entity -> toView(entity, Instant.now()))
                        .toList());
    }

    @Override
    public List<AccessAssignmentView> findEffectiveByUserAndPlatform(
            String userId,
            String platformCode,
            Instant now
    ) {
        return executor.query("AccessAssignment", () ->
                jpaRepository.findEffectiveByUserAndPlatform(userId, platformCode, now).stream()
                        .map(entity -> toView(entity, now))
                        .toList());
    }

    private AccessAssignmentView toView(AccessAssignmentEntity entity, Instant at) {
        AccessAssignmentStatus stored = entity.getStatus();
        AccessAssignmentStatus effective = stored;
        if ((stored == AccessAssignmentStatus.ACTIVE || stored == AccessAssignmentStatus.SUSPENDED)
                && entity.getExpiresAt() != null
                && !entity.getExpiresAt().isAfter(at)) {
            effective = AccessAssignmentStatus.EXPIRED;
        }
        return new AccessAssignmentView(
                entity.getUuid(),
                entity.getUser().getUuid(),
                entity.getPlatformRole().getPlatform().getCode(),
                entity.getPlatformRole().getRole().getCode(),
                entity.getScopeKey(),
                entity.getScopeId(),
                stored,
                effective,
                entity.getAssignedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getExpiresAt()
        );
    }
}
