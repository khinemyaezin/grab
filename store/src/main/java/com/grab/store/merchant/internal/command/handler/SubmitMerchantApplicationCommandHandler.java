package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.port.inbound.SubmitMerchantApplicationUseCase;
import com.merchant.application.model.write.SubmitMerchantApplicationCommand;
import com.merchant.application.model.write.MerchantAccountResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmitMerchantApplicationCommandHandler implements CommandHandler<SubmitMerchantApplicationCommand, MerchantAccountResult> {

    private final SubmitMerchantApplicationUseCase submitMerchantApplicationUseCase;

    @Override
    @MerchantTransactional
    public MerchantAccountResult handle(SubmitMerchantApplicationCommand command) {
        return submitMerchantApplicationUseCase.execute(command);
    }

    @Override
    public Class<SubmitMerchantApplicationCommand> getCommandType() {
        return SubmitMerchantApplicationCommand.class;
    }
}
