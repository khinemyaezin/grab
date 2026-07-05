package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.command.SubmitMerchantApplicationCommand;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.repository.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class SubmitMerchantApplicationCommandHandler
        implements CommandHandler<SubmitMerchantApplicationCommand, MerchantAccountResult> {

    private final MerchantAccountRepository merchants;

    @Override
    @MerchantTransactional
    public MerchantAccountResult handle(SubmitMerchantApplicationCommand command) {
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

    @Override
    public Class<SubmitMerchantApplicationCommand> getCommandType() {
        return SubmitMerchantApplicationCommand.class;
    }
}
