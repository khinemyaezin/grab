package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.SecurityManifestInboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityManifestInboxJpaRepository extends JpaRepository<SecurityManifestInboxEntity, String> {
    Optional<SecurityManifestInboxEntity> findTopByModuleKeyAndRevisionOrderByProcessedAtDesc(String moduleKey, int revision);
    Optional<SecurityManifestInboxEntity> findTopByModuleKeyOrderByRevisionDesc(String moduleKey);
}
