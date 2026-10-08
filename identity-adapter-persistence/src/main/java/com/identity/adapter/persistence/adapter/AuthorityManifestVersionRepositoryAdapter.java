package com.identity.adapter.persistence.adapter;

import com.identity.adapter.persistence.entity.AuthorityManifestVersionEntity;
import com.identity.adapter.persistence.repository.jpa.AuthorityManifestVersionJpaRepository;
import com.identity.domain.port.outbound.AuthorityManifestVersionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthorityManifestVersionRepositoryAdapter implements AuthorityManifestVersionRepository {
    private final AuthorityManifestVersionJpaRepository repository;

    @Override
    public boolean canApply(String moduleKey, int manifestVersion, String contentDigest) {
        return repository.findLockedByModuleKey(moduleKey)
                .map(current -> manifestVersion > current.getManifestVersion()
                        || (manifestVersion == current.getManifestVersion()
                        && contentDigest.equals(current.getContentDigest())))
                .orElse(true);
    }

    @Override
    public void recordApplied(String moduleKey, int manifestVersion, String contentDigest) {
        AuthorityManifestVersionEntity entity = repository.findLockedByModuleKey(moduleKey)
                .orElseGet(AuthorityManifestVersionEntity::new);
        entity.setModuleKey(moduleKey);
        entity.setManifestVersion(manifestVersion);
        entity.setContentDigest(contentDigest);
        repository.save(entity);
    }
}
