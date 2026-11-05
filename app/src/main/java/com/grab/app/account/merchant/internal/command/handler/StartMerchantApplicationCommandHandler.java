package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.StartMerchantApplicationCommand;
import com.merchant.application.port.inbound.StartMerchantApplicationUseCase;
import org.springframework.stereotype.Component;

@Component
public class StartMerchantApplicationCommandHandler
        implements CommandHandler<StartMerchantApplicationCommand, MerchantAccountResult> {

    private final StartMerchantApplicationUseCase startMerchantApplicationUseCase;

    public StartMerchantApplicationCommandHandler(StartMerchantApplicationUseCase startMerchantApplicationUseCase) {
        this.startMerchantApplicationUseCase = startMerchantApplicationUseCase;
    }

    @Override
    @MerchantTransactional
    public MerchantAccountResult handle(StartMerchantApplicationCommand command) {
        return startMerchantApplicationUseCase.execute(command);
    }

    @Override
    public Class<StartMerchantApplicationCommand> getCommandType() {
        return StartMerchantApplicationCommand.class;
    }
}

