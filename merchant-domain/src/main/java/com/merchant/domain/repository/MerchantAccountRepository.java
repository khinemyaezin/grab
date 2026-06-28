package com.merchant.domain.repository;

import com.grab.framework.id.Id;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.valueobject.BusinessRegistration;

import java.util.List;
import java.util.Optional;

public interface MerchantAccountRepository {
    Optional<MerchantAccount> findById(Id id);
    List<MerchantAccount> findByApplicantUserId(Id applicantUserId);
    List<MerchantAccount> findByStatus(MerchantStatus status);
    boolean existsOpenApplication(Id applicantUserId, MerchantType type);
    boolean existsRegistration(BusinessRegistration registration, Id excludingMerchantId);
    MerchantAccount save(MerchantAccount merchant);
}
