package com.saleschannel.adapter.persistence.repository.jpa;

import com.saleschannel.adapter.persistence.entity.SalesChannelSecurityManifestPublicationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesChannelSecurityManifestPublicationJpaRepository
        extends JpaRepository<SalesChannelSecurityManifestPublicationEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from SalesChannelSecurityManifestPublicationEntity state where state.moduleKey = :moduleKey")
    SalesChannelSecurityManifestPublicationEntity lockByModuleKey(@Param("moduleKey") String moduleKey);
}
