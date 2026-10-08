package com.identity.adapter.persistence.adapter;

import com.identity.adapter.persistence.entity.SecurityManifestInboxEntity;
import com.identity.adapter.persistence.repository.jpa.SecurityManifestInboxJpaRepository;
import com.identity.adapter.persistence.repository.jpa.SecurityManifestConflictJpaRepository;
import com.identity.domain.port.outbound.SecurityManifestInboxRepository;
import com.identity.domain.security.SecurityManifestCandidateStatus;
import com.identity.domain.security.SecurityManifestReceipt;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class SecurityManifestInboxRepositoryAdapter implements SecurityManifestInboxRepository {
    private final SecurityManifestInboxJpaRepository repository;
    private final SecurityManifestConflictJpaRepository conflicts;

    @Override
    public boolean alreadyProcessed(String eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public java.util.Optional<SecurityManifestReceipt> find(String eventId) {
        return repository.findById(eventId).map(entity -> new SecurityManifestReceipt(
                entity.getEventId(), entity.getModuleKey(), entity.getRevision(), entity.getContentDigest(),
                SecurityManifestCandidateStatus.valueOf(entity.getStatus()), entity.getErrorCode()));
    }

    @Override
    public int appliedRevision(String moduleKey) {
        return repository.findTopByModuleKeyOrderByRevisionDesc(moduleKey)
                .filter(entity -> SecurityManifestCandidateStatus.APPLIED.name().equals(entity.getStatus()))
                .map(SecurityManifestInboxEntity::getRevision)
                .orElse(0);
    }

    @Override
    public java.util.Optional<SecurityManifestReceipt> findByModuleRevision(String moduleKey, int revision) {
        return repository.findTopByModuleKeyAndRevisionOrderByProcessedAtDesc(moduleKey, revision)
                .map(entity -> new SecurityManifestReceipt(
                        entity.getEventId(), entity.getModuleKey(), entity.getRevision(), entity.getContentDigest(),
                        SecurityManifestCandidateStatus.valueOf(entity.getStatus()), entity.getErrorCode()));
    }

    @Override
    public void recordProcessed(String eventId, String moduleKey, int revision, String contentDigest) {
        recordOutcome(eventId, moduleKey, revision, contentDigest, SecurityManifestCandidateStatus.APPLIED, null);
    }

    @Override
    public void recordOutcome(String eventId, String moduleKey, int revision, String contentDigest,
                              SecurityManifestCandidateStatus status, String errorCode) {
        var existing = repository.findById(eventId);
        if (existing.isPresent()) {
            var entity = existing.get();
            if (!entity.getContentDigest().equals(contentDigest)) {
                var conflict = new com.identity.adapter.persistence.entity.SecurityManifestConflictEntity();
                conflict.setEventId(eventId);
                conflict.setModuleKey(moduleKey);
                conflict.setRevision(revision);
                conflict.setContentDigest(contentDigest);
                conflict.setStatus(status.name());
                conflict.setErrorCode(errorCode);
                conflict.setReceivedAt(Instant.now());
                conflicts.save(conflict);
                return;
            }
            entity.setStatus(status.name());
            entity.setErrorCode(errorCode);
            entity.setProcessedAt(Instant.now());
            repository.save(entity);
            return;
        }
        SecurityManifestInboxEntity entity = new SecurityManifestInboxEntity();
        entity.setEventId(eventId);
        entity.setModuleKey(moduleKey);
        entity.setRevision(revision);
        entity.setContentDigest(contentDigest);
        entity.setProcessedAt(Instant.now());
        entity.setStatus(status.name());
        entity.setErrorCode(errorCode);
        repository.save(entity);
    }
}
