package com.catalog.adapter.persistence.repository.jpa;

import com.catalog.adapter.persistence.entity.CatalogSecurityManifestPublicationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogSecurityManifestPublicationJpaRepository
        extends JpaRepository<CatalogSecurityManifestPublicationEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from CatalogSecurityManifestPublicationEntity state where state.moduleKey = :moduleKey")
    CatalogSecurityManifestPublicationEntity lockByModuleKey(@Param("moduleKey") String moduleKey);
}
