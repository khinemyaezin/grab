package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.UpdateMerchantProfileCommand;
import com.merchant.application.port.inbound.UpdateMerchantProfileUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdateMerchantProfileCommandHandler
        implements CommandHandler<UpdateMerchantProfileCommand, MerchantAccountResult> {

    private final UpdateMerchantProfileUseCase updateMerchantProfileUseCase;

    public UpdateMerchantProfileCommandHandler(UpdateMerchantProfileUseCase updateMerchantProfileUseCase) {
        this.updateMerchantProfileUseCase = updateMerchantProfileUseCase;
    }

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

