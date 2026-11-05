package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.CreateStorefrontCommand;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.port.inbound.CreateStorefrontUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreateStorefrontCommandHandler
        implements CommandHandler<CreateStorefrontCommand, StorefrontResult> {

    private final CreateStorefrontUseCase createStorefrontUseCase;

    public CreateStorefrontCommandHandler(CreateStorefrontUseCase createStorefrontUseCase) {
        this.createStorefrontUseCase = createStorefrontUseCase;
    }

    @Override
    @MerchantTransactional
    public StorefrontResult handle(CreateStorefrontCommand command) {
        return createStorefrontUseCase.execute(command);
    }

    @Override
    public Class<CreateStorefrontCommand> getCommandType() {
        return CreateStorefrontCommand.class;
    }
}

