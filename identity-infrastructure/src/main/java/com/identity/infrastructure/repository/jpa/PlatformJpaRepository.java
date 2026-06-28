package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.PlatformEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformJpaRepository extends JpaRepository<PlatformEntity, Long> {
    @EntityGraph(attributePaths = {"platformRoles", "platformRoles.role"})
    Optional<PlatformEntity> findByCode(String code);
}
