package com.merchant.application.service;

import com.merchant.application.port.inbound.SubmitMerchantApplicationUseCase;

import com.grab.framework.id.Id;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.SubmitMerchantApplicationCommand;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class SubmitMerchantApplicationService implements SubmitMerchantApplicationUseCase {

    private final MerchantAccountRepository merchants;
    public MerchantAccountResult execute(SubmitMerchantApplicationCommand command) {
        Id merchantId = command.merchantId();
        MerchantAccount merchant = merchants.findById(merchantId).orElseThrow(() -> notFound(merchantId));
        Id applicantId = command.applicantUserId();
        merchant.requireApplicant(applicantId);
        Instant now = Instant.now();
        merchant.submit(applicantId, now);
        MerchantAccount saved = merchants.save(merchant);
        return MerchantAccountResult.from(saved);
    }

    private MerchantServiceException notFound(Id merchantId) {
        MerchantServiceError error = new MerchantServiceError.MerchantNotFound(merchantId.getValue());
        return new MerchantServiceException(error, "Merchant account not found");
    }
}
