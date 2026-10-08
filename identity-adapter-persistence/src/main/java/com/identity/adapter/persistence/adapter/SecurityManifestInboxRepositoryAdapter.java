package com.identity.adapter.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.security.SecurityManifest;
import com.identity.adapter.persistence.entity.SecurityManifestConflictEntity;
import com.identity.adapter.persistence.entity.SecurityManifestInboxEntity;
import com.identity.adapter.persistence.repository.jpa.SecurityManifestInboxJpaRepository;
import com.identity.adapter.persistence.repository.jpa.SecurityManifestConflictJpaRepository;
import com.identity.domain.port.outbound.SecurityManifestInboxRepository;
import com.identity.domain.security.SecurityManifestCandidateStatus;
import com.identity.domain.security.SecurityManifestReceipt;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class SecurityManifestInboxRepositoryAdapter implements SecurityManifestInboxRepository {
    private final SecurityManifestInboxJpaRepository repository;
    private final SecurityManifestConflictJpaRepository conflicts;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<SecurityManifestReceipt> find(String eventId) {
        return repository.findById(eventId).map(entity -> new SecurityManifestReceipt(
                entity.getEventId(), entity.getModuleKey(), entity.getRevision(), entity.getContentDigest(),
                SecurityManifestCandidateStatus.valueOf(entity.getStatus()), entity.getErrorCode()));
    }

    @Override
    public void save(SecurityManifestReceipt receipt) {
        var existing = repository.findById(receipt.eventId());
        if (existing.isPresent()) {
            var entity = existing.get();
            if (!entity.getContentDigest().equals(receipt.contentDigest())) {
                throw new IllegalStateException("Security manifest receipt identity is immutable");
            }
            if (!SecurityManifestCandidateStatus.WAITING_DEPENDENCY.name().equals(entity.getStatus())) {
                return;
            }
            entity.setStatus(receipt.status().name());
            entity.setErrorCode(receipt.errorCode());
            repository.save(entity);
            return;
        }
        var entity = new SecurityManifestInboxEntity();
        entity.setEventId(receipt.eventId());
        entity.setModuleKey(receipt.moduleKey());
        entity.setRevision(receipt.revision());
        entity.setContentDigest(receipt.contentDigest());
        entity.setProcessedAt(Instant.now());
        entity.setStatus(receipt.status().name());
        entity.setErrorCode(receipt.errorCode());
        repository.save(entity);
    }

    @Override
    public void recordConflict(String eventId, SecurityManifest manifest, String suppliedDigest, String errorCode) {
        var entity = new SecurityManifestConflictEntity();
        entity.setEventId(eventId);
        entity.setModuleKey(manifest.moduleKey());
        entity.setRevision(manifest.securityRevision());
        entity.setContentDigest(manifest.contentDigest());
        entity.setSuppliedDigest(suppliedDigest);
        try {
            entity.setPayload(objectMapper.writeValueAsString(manifest));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Security manifest conflict could not be serialized", exception);
        }
        entity.setStatus(SecurityManifestCandidateStatus.QUARANTINED.name());
        entity.setErrorCode(errorCode);
        entity.setReceivedAt(Instant.now());
        conflicts.save(entity);
    }
}
