package com.merchant.application.port.outbound;

import com.merchant.application.model.read.MerchantAccountView;
import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;

import java.util.List;
import java.util.Optional;

public interface MerchantAccountQueryPort {
    Optional<MerchantAccountView> findById(String merchantId);

    List<MerchantAccountView> findByApplicantUserId(String applicantUserId);

    List<MerchantAccountView> findByStatus(MerchantStatus status);

    Optional<MerchantAccountView> findByApplicantUserIdAndType(String applicantUserId, MerchantType type);
}
