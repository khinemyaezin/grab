package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.RefreshSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;

public interface RefreshSessionJpaRepository extends JpaRepository<RefreshSessionEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshSessionEntity> findByTokenHash(String tokenHash);

    List<RefreshSessionEntity> findByTokenFamilyId(String tokenFamilyId);

    List<RefreshSessionEntity> findByUser_Uuid(String uuid);

    List<RefreshSessionEntity> findByAssignmentUuid(String assignmentUuid);

    List<RefreshSessionEntity> findByPlatformCodeAndScopeKeyAndScopeIdAndRevokedAtIsNull(
            String platformCode,
            String scopeKey,
            String scopeId
    );
}
