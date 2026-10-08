package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.AuthorityManifestVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface AuthorityManifestVersionJpaRepository extends JpaRepository<AuthorityManifestVersionEntity, Long> {
    Optional<AuthorityManifestVersionEntity> findByModuleKey(String moduleKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select manifest from AuthorityManifestVersionEntity manifest where manifest.moduleKey = :moduleKey")
    Optional<AuthorityManifestVersionEntity> findLockedByModuleKey(@Param("moduleKey") String moduleKey);
}
