package com.identity.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.identity.adapter.persistence.entity.AccessAssignmentEntity;
import com.identity.application.model.read.ScopeDisplayView;
import com.identity.adapter.persistence.repository.jpa.AccessAssignmentJpaRepository;
import com.identity.adapter.persistence.repository.jpa.ScopeDisplayJpaRepository;
import com.identity.application.model.read.AccessAssignmentView;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.domain.enums.AccessAssignmentStatus;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AccessAssignmentQueryAdapter implements AccessAssignmentQueryPort {
    private final AccessAssignmentJpaRepository jpaRepository;
    private final ScopeDisplayJpaRepository displays;
    private final PersistenceExecutor executor;

    @Override
    public List<AccessAssignmentView> findByUser(String userId) {
        return executor.query("AccessAssignment", () -> enrich(
                jpaRepository.findByUser_UuidOrderByCreatedAt(userId), Instant.now()));
    }

    @Override
    public List<AccessAssignmentView> findEffectiveByUser(String userId, Instant now) {
        return executor.query("AccessAssignment", () -> enrich(jpaRepository.findEffectiveByUser(userId, now), now));
    }

    private List<AccessAssignmentView> enrich(List<AccessAssignmentEntity> entities, Instant at) {
        if (entities.isEmpty()) {
            return List.of();
        }
        List<String> scopeKeys = entities.stream().map(AccessAssignmentEntity::getScopeKey).distinct().toList();
        List<String> scopeIds = entities.stream().map(AccessAssignmentEntity::getScopeId).distinct().toList();
        Map<ScopeKey, ScopeDisplayView> scopeDisplays = displays.findDisplays(scopeKeys, scopeIds).stream()
                .collect(Collectors.toMap(
                        display -> new ScopeKey(display.scopeKey(), display.scopeId()),
                        Function.identity(),
                        (first, ignored) -> first
                ));
        return entities.stream().map(entity -> {
            ScopeDisplayView display = scopeDisplays.get(new ScopeKey(entity.getScopeKey(), entity.getScopeId()));
            return toView(entity, at, display);
        }).toList();
    }

    private AccessAssignmentView toView(AccessAssignmentEntity entity, Instant at, ScopeDisplayView display) {
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
                entity.getRole().getCode(),
                entity.getScopeKey(),
                entity.getScopeId(),
                stored,
                effective,
                entity.getAssignedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getExpiresAt(),
                display == null ? null : display.title(),
                display == null ? null : display.subtitle()
        );
    }

    private record ScopeKey(String scopeKey, String scopeId) {
    }
}
