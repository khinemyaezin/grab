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
        List<String> accepted = List.of(SecurityManifestCandidateStatus.WAITING_DEPENDENCY.name(),
                SecurityManifestCandidateStatus.APPLIED.name());
        return repository.findTopByModuleKeyAndStatusInOrderByRevisionDesc(moduleKey, accepted)
                .map(SecurityManifestRevisionEntity::getRevision).orElse(0);
    }

    @Override
    public void save(SecurityManifestCandidate candidate) {
        var manifest = candidate.manifest();
        var existing = repository.findByModuleKeyAndRevision(manifest.moduleKey(), manifest.securityRevision());
        String digest = manifest.contentDigest();
        if (existing.isPresent() && !digest.equals(existing.get().getContentDigest())) {
            throw new IllegalStateException("Canonical security manifest payload is immutable");
        }
        SecurityManifestRevisionEntity entity = existing.orElseGet(SecurityManifestRevisionEntity::new);
        if (existing.isEmpty()) {
            entity.setEventId(candidate.eventId());
            entity.setModuleKey(manifest.moduleKey());
            entity.setRevision(manifest.securityRevision());
            entity.setContentDigest(digest);
            entity.setPayload(writePayload(manifest));
            entity.setReceivedAt(candidate.receivedAt());
        }
        entity.setStatus(candidate.status().name());
        entity.setDependencyError(candidate.errorCode());
        entity.setAppliedAt(candidate.appliedAt());
        repository.save(entity);
    }

    private SecurityManifestCandidate toCandidate(SecurityManifestRevisionEntity entity) {
        try {
            var manifest = objectMapper.readValue(entity.getPayload(), SecurityManifest.class);
            var status = SecurityManifestCandidateStatus.valueOf(entity.getStatus());
            return new SecurityManifestCandidate(entity.getEventId(), manifest, status,
                    entity.getDependencyError(), entity.getReceivedAt(), entity.getAppliedAt());
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
