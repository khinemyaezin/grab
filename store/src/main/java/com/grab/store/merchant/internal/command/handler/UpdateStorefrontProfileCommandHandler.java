package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.port.inbound.UpdateStorefrontProfileUseCase;
import com.merchant.application.model.write.UpdateStorefrontProfileCommand;
import com.merchant.application.model.write.StorefrontResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateStorefrontProfileCommandHandler implements CommandHandler<UpdateStorefrontProfileCommand, StorefrontResult> {

    private final UpdateStorefrontProfileUseCase updateStorefrontProfileUseCase;

    @Override
    @MerchantTransactional
    public StorefrontResult handle(UpdateStorefrontProfileCommand command) {
        return updateStorefrontProfileUseCase.execute(command);
    }

    @Override
    public Class<UpdateStorefrontProfileCommand> getCommandType() {
        return UpdateStorefrontProfileCommand.class;
    }
}
