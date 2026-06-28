package com.merchant.domain.service;

import com.grab.framework.id.Id;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.repository.MerchantAccountRepository;

import java.util.Objects;

public final class MerchantRegistrationPolicy {
    private final MerchantAccountRepository merchants;

    public MerchantRegistrationPolicy(MerchantAccountRepository merchants) {
        this.merchants = Objects.requireNonNull(merchants, "merchant repository is required");
    }

    public void requireNoOpenApplication(Id applicantUserId, MerchantType type) {
        if (merchants.existsOpenApplication(applicantUserId, type)) {
            throw new MerchantDomainException(
                    new MerchantDomainError.DuplicateOpenApplication(applicantUserId.getValue(), type.name()),
                    "An open merchant application already exists"
            );
        }
    }

    public void requireRegistrationAvailable(MerchantAccount merchant) {
        if (merchant.getRegistration() != null
                && merchants.existsRegistration(merchant.getRegistration(), merchant.getId())) {
            throw new MerchantDomainException(
                    new MerchantDomainError.DuplicateRegistration(
                            merchant.getRegistration().countryCode(),
                            merchant.getRegistration().registrationNumber()
                    ),
                    "Business registration is already used"
            );
        }
    }
}
