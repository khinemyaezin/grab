package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.SecurityManifestModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityManifestModuleJpaRepository extends JpaRepository<SecurityManifestModuleEntity, String> {
}
