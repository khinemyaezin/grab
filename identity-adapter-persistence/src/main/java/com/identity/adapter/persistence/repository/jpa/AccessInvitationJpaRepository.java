package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.AccessInvitationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessInvitationJpaRepository extends JpaRepository<AccessInvitationEntity, Long> {
    @EntityGraph(attributePaths = {"role"})
    Optional<AccessInvitationEntity> findByUuid(String uuid);

    @EntityGraph(attributePaths = {"role"})
    Optional<AccessInvitationEntity> findByTokenHash(String tokenHash);
}
