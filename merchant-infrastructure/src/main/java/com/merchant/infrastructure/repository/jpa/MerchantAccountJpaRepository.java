package com.merchant.infrastructure.repository.jpa;

import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.infrastructure.entity.MerchantAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MerchantAccountJpaRepository extends JpaRepository<MerchantAccountEntity, Long> {
    Optional<MerchantAccountEntity> findByUuid(String uuid);
    List<MerchantAccountEntity> findByApplicantUserIdOrderByCreatedAtDesc(String applicantUserId);
    Optional<MerchantAccountEntity> findByApplicantUserIdAndType(String applicantUserId, MerchantType type);
    List<MerchantAccountEntity> findByStatusOrderByCreatedAt(MerchantStatus status);

    @Query("""
            select (count(merchant) > 0) from MerchantAccountEntity merchant
            where merchant.applicantUserId = :applicantUserId
              and merchant.type = :type
              and merchant.status in (
                com.merchant.domain.enums.MerchantStatus.DRAFT,
                com.merchant.domain.enums.MerchantStatus.PENDING_REVIEW,
                com.merchant.domain.enums.MerchantStatus.CHANGES_REQUESTED,
                com.merchant.domain.enums.MerchantStatus.ACTIVE,
                com.merchant.domain.enums.MerchantStatus.SUSPENDED
              )
            """)
    boolean existsOpenApplication(@Param("applicantUserId") String applicantUserId,
                                  @Param("type") MerchantType type);

    @Query("""
            select (count(merchant) > 0) from MerchantAccountEntity merchant
            where merchant.registrationCountryCode = :countryCode
              and merchant.registrationNumber = :registrationNumber
              and merchant.uuid <> :excludingUuid
            """)
    boolean existsRegistration(@Param("countryCode") String countryCode,
                               @Param("registrationNumber") String registrationNumber,
                               @Param("excludingUuid") String excludingUuid);
}
