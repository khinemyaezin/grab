package com.merchant.adapter.persistence.repository.jpa;

import com.merchant.adapter.persistence.entity.MerchantMemberEntity;
import com.merchant.domain.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MerchantMemberJpaRepository extends JpaRepository<MerchantMemberEntity, Long> {
    Optional<MerchantMemberEntity> findByUuid(String uuid);

    Optional<MerchantMemberEntity> findByMerchantIdAndUserId(String merchantId, String userId);

    List<MerchantMemberEntity> findByMerchantIdOrderByCreatedAtAsc(String merchantId);

    boolean existsByMerchantIdAndUserId(String merchantId, String userId);

    @Query("""
            select count(m) from MerchantMemberEntity m
            where m.merchantId = :merchantId
              and m.role = :role
              and m.status = :status
            """)
    long countByMerchantIdAndRoleAndStatus(
            @Param("merchantId") String merchantId,
            @Param("role") String role,
            @Param("status") MemberStatus status
    );
}
