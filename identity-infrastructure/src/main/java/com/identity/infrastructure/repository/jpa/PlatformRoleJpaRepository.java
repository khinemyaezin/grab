package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.PlatformRoleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformRoleJpaRepository extends JpaRepository<PlatformRoleEntity, Long> {
    @EntityGraph(attributePaths = {"platform", "role", "role.authorities"})
    Optional<PlatformRoleEntity> findByPlatform_CodeAndRole_CodeAndActiveTrue(
            String platformCode,
            String roleCode
    );
}
