package com.identity.adapter.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.security.SecurityManifest;
import com.identity.adapter.persistence.entity.SecurityManifestRevisionEntity;
import com.identity.adapter.persistence.repository.jpa.SecurityManifestRevisionJpaRepository;
import com.identity.domain.port.outbound.SecurityManifestRevisionRepository;
import com.identity.domain.security.SecurityManifestCandidate;
import com.identity.domain.security.SecurityManifestCandidateStatus;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class SecurityManifestRevisionRepositoryAdapter implements SecurityManifestRevisionRepository {
    private final SecurityManifestRevisionJpaRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<SecurityManifestCandidate> find(String moduleKey, int revision) {
        return repository.findByModuleKeyAndRevision(moduleKey, revision).map(this::toCandidate);
    }

    @Override
    public int highestAcceptedRevision(String moduleKey) {
        return repository.findTopByModuleKeyOrderByRevisionDesc(moduleKey)
                .map(SecurityManifestRevisionEntity::getRevision).orElse(0);
    }

    @Override
    public List<SecurityManifestCandidate> findWaiting() {
        return repository.findByStatusOrderByReceivedAtAsc(SecurityManifestCandidateStatus.WAITING_DEPENDENCY.name())
                .stream().map(this::toCandidate).toList();
    }

    @Override
    public void record(String eventId, SecurityManifest manifest, SecurityManifestCandidateStatus status, String errorCode) {
        SecurityManifestRevisionEntity entity = repository.findByModuleKeyAndRevision(
                        manifest.moduleKey(), manifest.securityRevision())
                .orElseGet(SecurityManifestRevisionEntity::new);
        entity.setEventId(eventId);
        entity.setModuleKey(manifest.moduleKey());
        entity.setRevision(manifest.securityRevision());
        entity.setContentDigest(manifest.contentDigest());
        entity.setPayload(writePayload(manifest));
        entity.setStatus(status.name());
        entity.setDependencyError(errorCode);
        if (entity.getReceivedAt() == null) {
            entity.setReceivedAt(Instant.now());
        }
        if (status == SecurityManifestCandidateStatus.APPLIED && entity.getAppliedAt() == null) {
            entity.setAppliedAt(Instant.now());
        }
        repository.save(entity);
    }

    private SecurityManifestCandidate toCandidate(SecurityManifestRevisionEntity entity) {
        try {
            return new SecurityManifestCandidate(
                    entity.getEventId(), objectMapper.readValue(entity.getPayload(), SecurityManifest.class),
                    SecurityManifestCandidateStatus.valueOf(entity.getStatus()), entity.getDependencyError(),
                    entity.getReceivedAt(), entity.getAppliedAt());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored security manifest payload is invalid", exception);
        }
    }

    private String writePayload(SecurityManifest manifest) {
        try {
            return objectMapper.writeValueAsString(manifest);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Security manifest payload could not be serialized", exception);
        }
    }
}
