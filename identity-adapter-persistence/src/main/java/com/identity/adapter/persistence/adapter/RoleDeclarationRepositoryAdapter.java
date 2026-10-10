package com.identity.adapter.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.security.role.RoleDeclaration;
import com.identity.adapter.persistence.entity.RoleDeclarationConflictEntity;
import com.identity.adapter.persistence.entity.RoleDeclarationInboxEntity;
import com.identity.adapter.persistence.entity.RoleDeclarationRevisionEntity;
import com.identity.adapter.persistence.entity.RoleDeclarationStateEntity;
import com.identity.adapter.persistence.repository.jpa.RoleDeclarationConflictJpaRepository;
import com.identity.adapter.persistence.repository.jpa.RoleDeclarationInboxJpaRepository;
import com.identity.adapter.persistence.repository.jpa.RoleDeclarationRevisionJpaRepository;
import com.identity.adapter.persistence.repository.jpa.RoleDeclarationStateJpaRepository;
import com.identity.domain.port.outbound.RoleDeclarationRepository;
import com.identity.domain.security.RoleDeclarationCandidate;
import com.identity.domain.security.RoleDeclarationCandidateStatus;
import com.identity.domain.security.RoleDeclarationReceipt;
import com.identity.domain.security.RoleDeclarationState;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class RoleDeclarationRepositoryAdapter implements RoleDeclarationRepository {
    private static final List<String> ACCEPTED_STATUSES =
            List.of("RECEIVED", "WAITING_DEPENDENCY", "APPLIED");
    private final RoleDeclarationRevisionJpaRepository revisions;
    private final RoleDeclarationInboxJpaRepository inbox;
    private final RoleDeclarationConflictJpaRepository conflicts;
    private final RoleDeclarationStateJpaRepository states;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<RoleDeclarationCandidate> findCandidate(String owner, String roleCode, int revision) {
        return revisions.findByOwnerAndRoleCodeAndRevision(owner, roleCode, revision).map(this::toCandidate);
    }

    @Override
    public int highestAcceptedRevision(String owner, String roleCode) {
        return revisions.findTopByOwnerAndRoleCodeAndStatusInOrderByRevisionDesc(
                        owner, roleCode, ACCEPTED_STATUSES)
                .map(RoleDeclarationRevisionEntity::getRevision)
                .orElse(0);
    }

    @Override
    public void saveCandidate(RoleDeclarationCandidate candidate) {
        String owner = candidate.declaration().owner();
        String roleCode = candidate.declaration().roleCode();
        int revision = candidate.declaration().declarationRevision();
        Optional<RoleDeclarationRevisionEntity> existing =
                revisions.findByOwnerAndRoleCodeAndRevision(owner, roleCode, revision);
        RoleDeclarationRevisionEntity entity = existing.orElseGet(RoleDeclarationRevisionEntity::new);
        if (existing.isEmpty()) {
            entity.setOwner(owner);
            entity.setRoleCode(roleCode);
            entity.setRevision(revision);
            entity.setEventId(candidate.eventId());
            entity.setContentDigest(candidate.contentDigest());
            entity.setPayload(writePayload(candidate.declaration()));
            entity.setReceivedAt(candidate.receivedAt());
        } else if (!entity.getContentDigest().equals(candidate.contentDigest())) {
            throw new IllegalStateException("Role declaration revision payload is immutable");
        }
        entity.setStatus(candidate.status().name());
        entity.setReason(candidate.reason());
        entity.setAppliedAt(candidate.appliedAt());
        revisions.save(entity);
    }

    @Override
    public Optional<RoleDeclarationReceipt> findReceipt(String eventId) {
        return inbox.findById(eventId).map(this::toReceipt);
    }

    @Override
    public void saveReceipt(RoleDeclarationReceipt receipt) {
        RoleDeclarationInboxEntity entity = inbox.findById(receipt.eventId())
                .orElseGet(RoleDeclarationInboxEntity::new);
        entity.setEventId(receipt.eventId());
        entity.setOwner(receipt.owner());
        entity.setRoleCode(receipt.roleCode());
        entity.setRevision(receipt.revision());
        entity.setContentDigest(receipt.contentDigest());
        entity.setStatus(receipt.status().name());
        entity.setReceivedAt(receipt.receivedAt());
        inbox.save(entity);
    }

    @Override
    public Optional<RoleDeclarationState> findState(String roleCode) {
        return states.findById(roleCode).map(this::toState);
    }

    @Override
    public void saveState(RoleDeclarationState state) {
        RoleDeclarationStateEntity entity = states.findById(state.roleCode())
                .orElseGet(RoleDeclarationStateEntity::new);
        entity.setRoleCode(state.roleCode());
        entity.setOwner(state.owner());
        entity.setAssignmentScopeKey(state.assignmentScopeKey());
        entity.setAppliedRevision(state.appliedRevision());
        entity.setAppliedDigest(state.appliedDigest());
        states.save(entity);
    }

    @Override
    public void recordConflict(
            String eventId,
            String owner,
            String roleCode,
            int revision,
            String suppliedDigest,
            String existingDigest,
            String payload,
            String reason
    ) {
        if (conflicts.existsByEventIdAndSuppliedDigest(eventId, suppliedDigest)) {
            return;
        }
        RoleDeclarationConflictEntity entity = new RoleDeclarationConflictEntity();
        entity.setEventId(eventId);
        entity.setOwner(owner);
        entity.setRoleCode(roleCode);
        entity.setRevision(revision);
        entity.setSuppliedDigest(suppliedDigest);
        entity.setExistingDigest(existingDigest);
        entity.setPayload(payload);
        entity.setReason(reason);
        entity.setRecordedAt(Instant.now());
        conflicts.save(entity);
    }

    private RoleDeclarationCandidate toCandidate(RoleDeclarationRevisionEntity entity) {
        try {
            RoleDeclarationCandidate candidate = new RoleDeclarationCandidate(
                    entity.getEventId(),
                    objectMapper.readValue(entity.getPayload(), RoleDeclaration.class),
                    entity.getContentDigest(),
                    RoleDeclarationCandidateStatus.valueOf(entity.getStatus()),
                    entity.getReason(),
                    entity.getReceivedAt(),
                    entity.getAppliedAt()
            );
            return candidate;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored role declaration is invalid", exception);
        }
    }

    private RoleDeclarationReceipt toReceipt(RoleDeclarationInboxEntity entity) {
        RoleDeclarationReceipt receipt = new RoleDeclarationReceipt(
                entity.getEventId(),
                entity.getOwner(),
                entity.getRoleCode(),
                entity.getRevision(),
                entity.getContentDigest(),
                RoleDeclarationCandidateStatus.valueOf(entity.getStatus()),
                entity.getReceivedAt()
        );
        return receipt;
    }

    private RoleDeclarationState toState(RoleDeclarationStateEntity entity) {
        RoleDeclarationState state = new RoleDeclarationState(
                entity.getRoleCode(),
                entity.getOwner(),
                entity.getAssignmentScopeKey(),
                entity.getAppliedRevision(),
                entity.getAppliedDigest()
        );
        return state;
    }

    private String writePayload(RoleDeclaration declaration) {
        try {
            String payload = objectMapper.writeValueAsString(declaration);
            return payload;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Role declaration could not be serialized", exception);
        }
    }
}
