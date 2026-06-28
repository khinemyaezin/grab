package com.merchant.domain.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.domain.valueobject.BusinessRegistration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerchantRegistrationPolicyTest {
    @Test
    void requireNoOpenApplication_whenRepositoryFindsConflict_shouldReject() {
        MerchantRegistrationPolicy policy = new MerchantRegistrationPolicy(repository(true, false));

        assertThatThrownBy(() -> policy.requireNoOpenApplication(
                new CommonId("applicant-1"), MerchantType.RETAILER
        )).isInstanceOf(MerchantDomainException.class);
    }

    private MerchantAccountRepository repository(boolean open, boolean registration) {
        return new MerchantAccountRepository() {
            public Optional<MerchantAccount> findById(Id id) { return Optional.empty(); }
            public List<MerchantAccount> findByApplicantUserId(Id id) { return List.of(); }
            public List<MerchantAccount> findByStatus(MerchantStatus status) { return List.of(); }
            public boolean existsOpenApplication(Id id, MerchantType type) { return open; }
            public boolean existsRegistration(BusinessRegistration value, Id excluded) { return registration; }
            public MerchantAccount save(MerchantAccount merchant) { return merchant; }
        };
    }
}
