package com.merchant.application.service;

import com.merchant.application.port.inbound.StartMerchantApplicationUseCase;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.Id;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.StartMerchantApplicationCommand;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.service.MerchantRegistrationPolicy;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class StartMerchantApplicationService implements StartMerchantApplicationUseCase {

    private final MerchantAccountRepository merchants;
    private final MerchantRegistrationPolicy registrationPolicy;
    private final IdGenerator ids;
    public MerchantAccountResult execute(StartMerchantApplicationCommand command) {
        Id applicantId = command.applicantUserId();
        registrationPolicy.requireNoOpenApplication(applicantId, command.type());

        Id merchantId = ids.generateId();
        Instant now = Instant.now();
        MerchantAccount merchant = MerchantAccount.startDraft(
                merchantId,
                applicantId,
                command.type(),
                command.displayName(),
                now);
        MerchantAccount saved = merchants.save(merchant);
        return MerchantAccountResult.from(saved);
    }
}
