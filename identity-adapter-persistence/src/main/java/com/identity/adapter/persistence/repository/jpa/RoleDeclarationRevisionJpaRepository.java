package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.RoleDeclarationRevisionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleDeclarationRevisionJpaRepository
        extends JpaRepository<RoleDeclarationRevisionEntity, Long> {
    Optional<RoleDeclarationRevisionEntity> findByOwnerAndRoleCodeAndRevision(
            String owner, String roleCode, int revision);

    Optional<RoleDeclarationRevisionEntity> findTopByOwnerAndRoleCodeAndStatusInOrderByRevisionDesc(
            String owner, String roleCode, List<String> statuses);

    List<RoleDeclarationRevisionEntity> findByStatusOrderByReceivedAtAsc(
            String status, Pageable pageable);

    Optional<RoleDeclarationRevisionEntity> findTopByRoleCodeOrderByRevisionDesc(String roleCode);

    long countByRoleCodeAndStatus(String roleCode, String status);
}
