package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.RoleDeclarationConflictEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDeclarationConflictJpaRepository
        extends JpaRepository<RoleDeclarationConflictEntity, Long> {
    boolean existsByEventIdAndSuppliedDigest(String eventId, String suppliedDigest);

    long countByRoleCode(String roleCode);
}
