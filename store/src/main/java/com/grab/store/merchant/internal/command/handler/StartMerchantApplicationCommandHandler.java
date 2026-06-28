package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.command.StartMerchantApplicationCommand;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.domain.service.MerchantRegistrationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class StartMerchantApplicationCommandHandler
        implements CommandHandler<StartMerchantApplicationCommand, MerchantAccountResult> {

    private final MerchantAccountRepository merchants;
    private final MerchantRegistrationPolicy registrationPolicy;
    private final IdGenerator ids;

    @Override
    @MerchantTransactional
    public MerchantAccountResult handle(StartMerchantApplicationCommand command) {
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

    @Override
    public Class<StartMerchantApplicationCommand> getCommandType() {
        return StartMerchantApplicationCommand.class;
    }
}
