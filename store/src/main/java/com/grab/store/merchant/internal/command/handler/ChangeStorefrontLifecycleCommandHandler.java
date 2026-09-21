package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.port.inbound.ChangeStorefrontLifecycleUseCase;
import com.merchant.application.model.write.ChangeStorefrontLifecycleCommand;
import com.merchant.application.model.write.StorefrontResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeStorefrontLifecycleCommandHandler implements CommandHandler<ChangeStorefrontLifecycleCommand, StorefrontResult> {

    private final ChangeStorefrontLifecycleUseCase changeStorefrontLifecycleUseCase;

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
