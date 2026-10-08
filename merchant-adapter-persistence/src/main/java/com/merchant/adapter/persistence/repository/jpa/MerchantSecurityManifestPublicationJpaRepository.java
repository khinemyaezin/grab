package com.merchant.adapter.persistence.repository.jpa;

import com.merchant.adapter.persistence.entity.MerchantSecurityManifestPublicationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerchantSecurityManifestPublicationJpaRepository
        extends JpaRepository<MerchantSecurityManifestPublicationEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from MerchantSecurityManifestPublicationEntity state where state.moduleKey = :moduleKey")
    MerchantSecurityManifestPublicationEntity lockByModuleKey(@Param("moduleKey") String moduleKey);
}
