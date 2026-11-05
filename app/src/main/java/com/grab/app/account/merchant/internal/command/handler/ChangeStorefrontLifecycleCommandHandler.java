package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.ChangeStorefrontLifecycleCommand;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.port.inbound.ChangeStorefrontLifecycleUseCase;
import org.springframework.stereotype.Component;

@Component
public class ChangeStorefrontLifecycleCommandHandler
        implements CommandHandler<ChangeStorefrontLifecycleCommand, StorefrontResult> {

    private final ChangeStorefrontLifecycleUseCase changeStorefrontLifecycleUseCase;

    public ChangeStorefrontLifecycleCommandHandler(ChangeStorefrontLifecycleUseCase changeStorefrontLifecycleUseCase) {
        this.changeStorefrontLifecycleUseCase = changeStorefrontLifecycleUseCase;
    }

    @Override
    @MerchantTransactional
    public StorefrontResult handle(ChangeStorefrontLifecycleCommand command) {
        return changeStorefrontLifecycleUseCase.execute(command);
    }

    @Override
    public Class<ChangeStorefrontLifecycleCommand> getCommandType() {
        return ChangeStorefrontLifecycleCommand.class;
    }
}

