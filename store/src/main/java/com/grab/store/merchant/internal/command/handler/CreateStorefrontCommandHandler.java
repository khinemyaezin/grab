package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.port.inbound.CreateStorefrontUseCase;
import com.merchant.application.model.write.CreateStorefrontCommand;
import com.merchant.application.model.write.StorefrontResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateStorefrontCommandHandler implements CommandHandler<CreateStorefrontCommand, StorefrontResult> {

    private final CreateStorefrontUseCase createStorefrontUseCase;

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
