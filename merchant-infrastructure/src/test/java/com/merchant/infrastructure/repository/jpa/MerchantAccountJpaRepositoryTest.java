package com.merchant.infrastructure.repository.jpa;

import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.infrastructure.entity.MerchantAccountEntity;
import com.merchant.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class MerchantAccountJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private MerchantAccountJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        repository.save(createEntity("m1", "user1", MerchantType.RETAILER, MerchantStatus.ACTIVE, "US", "REG123"));
        repository.save(createEntity("m2", "user1", MerchantType.RETAILER, MerchantStatus.DRAFT, "US", "REG124"));
        repository.save(createEntity("m3", "user2", MerchantType.THIRD_PARTY, MerchantStatus.PENDING_REVIEW, "UK", "REG999"));
    }

    private MerchantAccountEntity createEntity(String uuid, String applicantId, MerchantType type, MerchantStatus status, String countryCode, String regNum) {
        MerchantAccountEntity entity = new MerchantAccountEntity();
        entity.setUuid(uuid);
        entity.setDisplayName("");
        entity.setApplicantUserId(applicantId);
        entity.setType(type);
        entity.setStatus(status);
        entity.setRegistrationCountryCode(countryCode);
        entity.setRegistrationNumber(regNum);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setVersion(0L);
        return entity;
    }

    @Test
    void findByUuid_withValidUuid_shouldReturnEntity() {
        Optional<MerchantAccountEntity> result = repository.findByUuid("m1");
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("user1", result.get().getApplicantUserId());
    }

    @Test
    void findByApplicantUserIdOrderByCreatedAtDesc_withValidApplicantId_shouldReturnEntitiesInOrder() {
        List<MerchantAccountEntity> results = repository.findByApplicantUserIdOrderByCreatedAtDesc("user1");
        Assertions.assertEquals(2, results.size());
    }

    @Test
    void findByStatusOrderByCreatedAt_withValidStatus_shouldReturnEntitiesInOrder() {
        List<MerchantAccountEntity> results = repository.findByStatusOrderByCreatedAt(MerchantStatus.ACTIVE);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("m1", results.get(0).getUuid());
    }

    @Test
    void existsOpenApplication_withOpenApplication_shouldReturnTrueAndFalseAppropriately() {
        boolean exists = repository.existsOpenApplication("user1", MerchantType.RETAILER);
        Assertions.assertTrue(exists);

        boolean exists2 = repository.existsOpenApplication("user3", MerchantType.RETAILER);
        Assertions.assertFalse(exists2);
    }

    @Test
    void existsRegistration_withExistingRegistration_shouldReturnTrueAndFalseAppropriately() {
        boolean exists = repository.existsRegistration("US", "REG123", "m2");
        Assertions.assertTrue(exists);

        boolean existsSelf = repository.existsRegistration("US", "REG123", "m1");
        Assertions.assertFalse(existsSelf);
    }
}
