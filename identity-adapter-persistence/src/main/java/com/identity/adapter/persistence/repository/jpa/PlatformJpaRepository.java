package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.PlatformEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface PlatformJpaRepository extends JpaRepository<PlatformEntity, Long> {
    @EntityGraph(attributePaths = {"platformRoles", "platformRoles.role", "authorities"})
    Optional<PlatformEntity> findByCode(String code);

    @EntityGraph(attributePaths = {"platformRoles", "platformRoles.role", "authorities"})
    List<PlatformEntity> findDistinctByPlatformRoles_Role_CodeAndPlatformRoles_ActiveTrue(String roleCode);
}
