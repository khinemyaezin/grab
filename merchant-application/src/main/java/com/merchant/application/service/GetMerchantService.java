package com.merchant.application.service;

import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.port.inbound.GetMerchantUseCase;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.application.model.read.GetMerchantQuery;
import com.merchant.application.model.read.MerchantAccountView;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetMerchantService implements GetMerchantUseCase {
    private final MerchantAccountQueryPort merchants;

    public MerchantAccountResult execute(GetMerchantQuery query) {
        MerchantAccountView merchant = merchants.findById(query.merchantId().getValue())
                .orElseThrow(() -> notFound(query.merchantId().getValue()));
        if (!query.reviewerAccess() && !query.scopedAccess()) {
            requireApplicant(merchant, query.actorId().getValue());
        }
        return MerchantAccountResult.from(merchant);
    }

    private void requireApplicant(MerchantAccountView merchant, String actorUserId) {
        if (!merchant.applicantUserId().equals(actorUserId)) {
            throw new MerchantServiceException(
                    new MerchantServiceError.ApplicantAccessForbidden(merchant.merchantId()),
                    "Merchant application belongs to another user"
            );
        }
    }

    private MerchantServiceException notFound(String merchantId) {
        MerchantServiceError error = new MerchantServiceError.MerchantNotFound(merchantId);
        return new MerchantServiceException(error, "Merchant account not found");
    }
}
