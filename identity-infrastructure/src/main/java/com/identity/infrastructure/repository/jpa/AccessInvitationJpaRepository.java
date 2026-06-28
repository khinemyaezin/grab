package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.AccessInvitationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessInvitationJpaRepository extends JpaRepository<AccessInvitationEntity, Long> {
    @EntityGraph(attributePaths = {"platformRole", "platformRole.platform", "platformRole.role"})
    Optional<AccessInvitationEntity> findByUuid(String uuid);

    @EntityGraph(attributePaths = {"platformRole", "platformRole.platform", "platformRole.role"})
    Optional<AccessInvitationEntity> findByTokenHash(String tokenHash);
}
