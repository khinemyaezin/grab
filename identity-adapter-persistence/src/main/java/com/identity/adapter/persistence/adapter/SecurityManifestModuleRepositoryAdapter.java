package com.identity.adapter.persistence.adapter;

import com.identity.adapter.persistence.entity.SecurityManifestModuleEntity;
import com.identity.adapter.persistence.repository.jpa.SecurityManifestModuleJpaRepository;
import com.identity.domain.port.outbound.SecurityManifestModuleRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecurityManifestModuleRepositoryAdapter implements SecurityManifestModuleRepository {
    private final SecurityManifestModuleJpaRepository repository;

    @Override
    public int appliedRevision(String moduleKey) {
        return repository.findById(moduleKey).map(SecurityManifestModuleEntity::getAppliedRevision).orElse(0);
    }

    @Override
    public void recordApplied(String moduleKey, int revision, String digest) {
        var entity = repository.findById(moduleKey).orElseGet(SecurityManifestModuleEntity::new);
        entity.setModuleKey(moduleKey);
        entity.setAppliedRevision(revision);
        entity.setAppliedDigest(digest);
        repository.save(entity);
    }
}
