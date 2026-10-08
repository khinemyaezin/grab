package com.merchant.adapter.persistence.repository.jpa;

import com.merchant.adapter.persistence.entity.MerchantMemberEntity;
import com.merchant.adapter.persistence.repository.jpa.config.RepositoryTestConfig;
import com.merchant.domain.enums.MemberStatus;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MerchantMemberJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private MerchantMemberJpaRepository repository;

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.save(member("mem-1", "mer-1", "usr-1", "MERCHANT_ADMIN", MemberStatus.ACTIVE));
    }

    @Test
    void findByMerchantIdAndUserId_shouldReturnMember() {
        Optional<MerchantMemberEntity> result = repository.findByMerchantIdAndUserId("mer-1", "usr-1");

        assertThat(result).isPresent();
        assertThat(result.get().getRole()).isEqualTo("MERCHANT_ADMIN");
        assertThat(result.get().getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void countByMerchantIdAndRoleAndStatus_shouldReturnAccurateCount() {
        long count = repository.countByMerchantIdAndRoleAndStatus("mer-1", "MERCHANT_ADMIN", MemberStatus.ACTIVE);
        assertThat(count).isEqualTo(1L);

        long nonExistent = repository.countByMerchantIdAndRoleAndStatus("mer-1", "OPERATOR", MemberStatus.ACTIVE);
        assertThat(nonExistent).isEqualTo(0L);
    }

    @Test
    void save_withDuplicateMerchantIdAndUserId_shouldFail() {
        assertThrows(ConstraintViolationException.class, () ->
                repository.saveAndFlush(member("mem-2", "mer-1", "usr-1", "OPERATOR", MemberStatus.INVITED)));
    }

    private MerchantMemberEntity member(
            String uuid,
            String merchantId,
            String userId,
            String role,
            MemberStatus status
    ) {
        MerchantMemberEntity entity = new MerchantMemberEntity();
        entity.setUuid(uuid);
        entity.setMerchantId(merchantId);
        entity.setUserId(userId);
        entity.setRole(role);
        entity.setAuthorities(Set.of("AUTH_1"));
        entity.setStatus(status);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setVersion(0L);
        return entity;
    }
}
