package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.port.inbound.StartMerchantApplicationUseCase;
import com.merchant.application.model.write.StartMerchantApplicationCommand;
import com.merchant.application.model.write.MerchantAccountResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartMerchantApplicationCommandHandler implements CommandHandler<StartMerchantApplicationCommand, MerchantAccountResult> {

    private final StartMerchantApplicationUseCase startMerchantApplicationUseCase;

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
