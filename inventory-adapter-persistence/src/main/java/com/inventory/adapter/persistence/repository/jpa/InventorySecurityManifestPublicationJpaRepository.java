package com.inventory.adapter.persistence.repository.jpa;

import com.inventory.adapter.persistence.entity.InventorySecurityManifestPublicationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventorySecurityManifestPublicationJpaRepository
        extends JpaRepository<InventorySecurityManifestPublicationEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from InventorySecurityManifestPublicationEntity state where state.moduleKey = :moduleKey")
    InventorySecurityManifestPublicationEntity lockByModuleKey(@Param("moduleKey") String moduleKey);
}
