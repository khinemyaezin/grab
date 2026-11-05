package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.ChangeMerchantLifecycleCommand;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.port.inbound.ChangeMerchantLifecycleUseCase;
import org.springframework.stereotype.Component;

@Component
public class ChangeMerchantLifecycleCommandHandler
        implements CommandHandler<ChangeMerchantLifecycleCommand, MerchantAccountResult> {

    private final ChangeMerchantLifecycleUseCase changeMerchantLifecycleUseCase;

    public ChangeMerchantLifecycleCommandHandler(ChangeMerchantLifecycleUseCase changeMerchantLifecycleUseCase) {
        this.changeMerchantLifecycleUseCase = changeMerchantLifecycleUseCase;
    }

    @Override
    @MerchantTransactional
    public MerchantAccountResult handle(ChangeMerchantLifecycleCommand command) {
        return changeMerchantLifecycleUseCase.execute(command);
    }

    @Override
    public Class<ChangeMerchantLifecycleCommand> getCommandType() {
        return ChangeMerchantLifecycleCommand.class;
    }
}

