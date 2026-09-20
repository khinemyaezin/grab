package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.port.inbound.UpdateMerchantProfileUseCase;
import com.merchant.application.model.write.UpdateMerchantProfileCommand;
import com.merchant.application.model.write.MerchantAccountResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateMerchantProfileCommandHandler implements CommandHandler<UpdateMerchantProfileCommand, MerchantAccountResult> {

    private final UpdateMerchantProfileUseCase updateMerchantProfileUseCase;

    @Override
    @MerchantTransactional
    public MerchantAccountResult handle(UpdateMerchantProfileCommand command) {
        return updateMerchantProfileUseCase.execute(command);
    }

    @Override
    public Class<UpdateMerchantProfileCommand> getCommandType() {
        return UpdateMerchantProfileCommand.class;
    }
}
