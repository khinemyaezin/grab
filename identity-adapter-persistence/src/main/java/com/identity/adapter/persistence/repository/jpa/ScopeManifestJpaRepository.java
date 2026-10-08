package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.ScopeManifestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Optional;

public interface ScopeManifestJpaRepository extends JpaRepository<ScopeManifestEntity, Long> {
    List<ScopeManifestEntity> findByModuleKey(String moduleKey);

    Optional<ScopeManifestEntity> findTopByModuleKeyOrderByManifestVersionDesc(String moduleKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select scope from ScopeManifestEntity scope where scope.moduleKey = :moduleKey")
    List<ScopeManifestEntity> findByModuleKeyForUpdate(@Param("moduleKey") String moduleKey);

    boolean existsByScopeKeyAndActiveTrue(String scopeKey);

    Optional<ScopeManifestEntity> findByScopeKey(String scopeKey);

    List<ScopeManifestEntity> findByActiveTrue();
}
