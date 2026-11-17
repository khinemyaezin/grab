package com.grab.service.account.merchant.internal.command.handler;

import com.grab.service.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.ChangeMerchantLifecycleCommand;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.port.inbound.ChangeMerchantLifecycleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeMerchantLifecycleCommandHandler
        implements CommandHandler<ChangeMerchantLifecycleCommand, MerchantAccountResult> {

    private final ChangeMerchantLifecycleUseCase changeMerchantLifecycleUseCase;

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
