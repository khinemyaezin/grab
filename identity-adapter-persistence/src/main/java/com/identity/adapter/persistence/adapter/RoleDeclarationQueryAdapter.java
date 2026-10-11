package com.identity.adapter.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.security.role.RoleDeclaration;
import com.identity.adapter.persistence.entity.RoleDeclarationStateEntity;
import com.identity.adapter.persistence.entity.RoleDeclarationRevisionEntity;
import com.identity.adapter.persistence.repository.jpa.RoleDeclarationConflictJpaRepository;
import com.identity.adapter.persistence.repository.jpa.RoleDeclarationRevisionJpaRepository;
import com.identity.adapter.persistence.repository.jpa.RoleDeclarationStateJpaRepository;
import com.identity.application.model.read.RoleDeclarationStatusView;
import com.identity.application.model.read.WaitingRoleDeclarationView;
import com.identity.application.port.outbound.RoleDeclarationQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class RoleDeclarationQueryAdapter implements RoleDeclarationQueryPort {
    private final RoleDeclarationRevisionJpaRepository revisions;
    private final RoleDeclarationStateJpaRepository states;
    private final RoleDeclarationConflictJpaRepository conflicts;
    private final ObjectMapper objectMapper;

    @Override
    public List<WaitingRoleDeclarationView> findWaiting(int limit) {
        List<RoleDeclarationRevisionEntity> waiting =
                revisions.findByStatusOrderByReceivedAtAsc("WAITING_DEPENDENCY", PageRequest.of(0, limit));
        return waiting.stream().map(this::toWaitingView).toList();
    }

    @Override
    public RoleDeclarationStatusView status(String roleCode) {
        Optional<RoleDeclarationStateEntity> state = states.findById(roleCode);
        Optional<RoleDeclarationRevisionEntity> latest =
                revisions.findTopByRoleCodeOrderByRevisionDesc(roleCode);
        int appliedRevision = state.map(entity -> entity.getAppliedRevision()).orElse(0);
        String appliedDigest = state.map(entity -> entity.getAppliedDigest()).orElse("");
        String owner = state.map(entity -> entity.getOwner())
                .orElseGet(() -> latest.map(entity -> entity.getOwner()).orElse(null));
        int latestRevision = latest.map(entity -> entity.getRevision()).orElse(0);
        String latestStatus = latest.map(entity -> entity.getStatus()).orElse("UNDECLARED");
        String reason = latest.map(entity -> entity.getReason()).orElse(null);
        long conflictCount = conflicts.countByRoleCode(roleCode);
        RoleDeclarationStatusView view = new RoleDeclarationStatusView(
                roleCode, owner, appliedRevision, appliedDigest, latestRevision, latestStatus, reason, conflictCount);
        return view;
    }

    private WaitingRoleDeclarationView toWaitingView(RoleDeclarationRevisionEntity entity) {
        try {
            RoleDeclaration declaration = objectMapper.readValue(entity.getPayload(), RoleDeclaration.class);
            WaitingRoleDeclarationView view = new WaitingRoleDeclarationView(
                    declaration, entity.getEventId(), entity.getContentDigest(), entity.getReceivedAt());
            return view;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored role declaration is invalid", exception);
        }
    }
}
