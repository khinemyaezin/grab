package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.AutoApproveMerchantApplicationCommand;
import com.merchant.application.port.inbound.AutoApproveMerchantApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoApproveMerchantApplicationCommandHandler
        implements CommandHandler<AutoApproveMerchantApplicationCommand, Void> {

    private final AutoApproveMerchantApplicationUseCase useCase;

    @Override
    @MerchantTransactional
    public Void handle(AutoApproveMerchantApplicationCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<AutoApproveMerchantApplicationCommand> getCommandType() {
        return AutoApproveMerchantApplicationCommand.class;
    }
}
