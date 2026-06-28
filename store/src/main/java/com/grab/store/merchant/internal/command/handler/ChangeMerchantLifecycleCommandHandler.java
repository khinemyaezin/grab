package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.command.ChangeMerchantLifecycleCommand;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.domain.valueobject.LifecycleReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class ChangeMerchantLifecycleCommandHandler
        implements CommandHandler<ChangeMerchantLifecycleCommand, MerchantAccountResult> {

    private final MerchantAccountRepository merchants;

    @Override
    @MerchantTransactional
    public MerchantAccountResult handle(ChangeMerchantLifecycleCommand command) {
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

    @Override
    public Class<ChangeMerchantLifecycleCommand> getCommandType() {
        return ChangeMerchantLifecycleCommand.class;
    }
}
