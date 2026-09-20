package com.merchant.application.service;

import com.merchant.application.port.inbound.ChangeMerchantLifecycleUseCase;

import com.grab.framework.id.Id;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.ChangeMerchantLifecycleCommand;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.valueobject.LifecycleReason;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class ChangeMerchantLifecycleService implements ChangeMerchantLifecycleUseCase {

    private final MerchantAccountRepository merchants;
    public MerchantAccountResult execute(ChangeMerchantLifecycleCommand command) {
        Id merchantId = command.merchantId();
        MerchantAccount merchant = merchants.findById(merchantId).orElseThrow(() -> notFound(merchantId));
        Id actorId = command.actorId();
        Instant now = Instant.now();

        switch (command.action()) {
            case REQUEST_CHANGES -> merchant.requestChanges(actorId, reason(command), now);
            case APPROVE -> merchant.approve(actorId, now);
            case REJECT -> merchant.reject(actorId, reason(command), now);
            case SUSPEND -> merchant.suspend(actorId, reason(command), now);
            case REACTIVATE -> merchant.reactivate(actorId, now);
            case CLOSE -> merchant.close(actorId, reason(command), now);
        }
        MerchantAccount saved = merchants.save(merchant);
        return MerchantAccountResult.from(saved);
    }

    private MerchantServiceException notFound(Id merchantId) {
        MerchantServiceError error = new MerchantServiceError.MerchantNotFound(merchantId.getValue());
        return new MerchantServiceException(error, "Merchant account not found");
    }

    private LifecycleReason reason(ChangeMerchantLifecycleCommand command) {
        return new LifecycleReason(command.reason());
    }
}
