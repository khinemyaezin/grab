package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.SecurityManifestConflictEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityManifestConflictJpaRepository extends JpaRepository<SecurityManifestConflictEntity, Long> {
}
