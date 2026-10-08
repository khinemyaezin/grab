package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.SecurityManifestRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;

public interface SecurityManifestRevisionJpaRepository extends JpaRepository<SecurityManifestRevisionEntity, Long> {
    Optional<SecurityManifestRevisionEntity> findByModuleKeyAndRevision(String moduleKey, int revision);
    Optional<SecurityManifestRevisionEntity> findTopByModuleKeyAndStatusOrderByReceivedAtAsc(String moduleKey, String status);
    Optional<SecurityManifestRevisionEntity> findTopByModuleKeyAndStatusInOrderByRevisionDesc(
            String moduleKey, Collection<String> statuses);
}
