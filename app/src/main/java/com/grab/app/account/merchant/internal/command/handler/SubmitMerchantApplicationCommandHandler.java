package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.SubmitMerchantApplicationCommand;
import com.merchant.application.port.inbound.SubmitMerchantApplicationUseCase;
import org.springframework.stereotype.Component;

@Component
public class SubmitMerchantApplicationCommandHandler
        implements CommandHandler<SubmitMerchantApplicationCommand, MerchantAccountResult> {

    private final SubmitMerchantApplicationUseCase submitMerchantApplicationUseCase;

    public SubmitMerchantApplicationCommandHandler(SubmitMerchantApplicationUseCase submitMerchantApplicationUseCase) {
        this.submitMerchantApplicationUseCase = submitMerchantApplicationUseCase;
    }

    @Override
    @MerchantTransactional
    public MerchantAccountResult handle(SubmitMerchantApplicationCommand command) {
        return submitMerchantApplicationUseCase.execute(command);
    }

    @Override
    public Class<SubmitMerchantApplicationCommand> getCommandType() {
        return SubmitMerchantApplicationCommand.class;
    }
}

