package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.SecurityManifestRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SecurityManifestRevisionJpaRepository extends JpaRepository<SecurityManifestRevisionEntity, Long> {
    Optional<SecurityManifestRevisionEntity> findByModuleKeyAndRevision(String moduleKey, int revision);
    List<SecurityManifestRevisionEntity> findByStatusOrderByReceivedAtAsc(String status);
    Optional<SecurityManifestRevisionEntity> findTopByModuleKeyOrderByRevisionDesc(String moduleKey);
}
