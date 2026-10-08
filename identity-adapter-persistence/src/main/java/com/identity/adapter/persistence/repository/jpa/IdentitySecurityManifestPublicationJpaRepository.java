package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.IdentitySecurityManifestPublicationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdentitySecurityManifestPublicationJpaRepository
        extends JpaRepository<IdentitySecurityManifestPublicationEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from IdentitySecurityManifestPublicationEntity state where state.moduleKey = :moduleKey")
    IdentitySecurityManifestPublicationEntity lockByModuleKey(@Param("moduleKey") String moduleKey);
}
