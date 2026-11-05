package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RemoveMerchantMemberCommand;
import com.merchant.application.port.inbound.RemoveMerchantMemberUseCase;
import org.springframework.stereotype.Component;

@Component
public class RemoveMerchantMemberCommandHandler
        implements CommandHandler<RemoveMerchantMemberCommand, MerchantMemberResult> {

    private final RemoveMerchantMemberUseCase removeMerchantMemberUseCase;

    public RemoveMerchantMemberCommandHandler(RemoveMerchantMemberUseCase removeMerchantMemberUseCase) {
        this.removeMerchantMemberUseCase = removeMerchantMemberUseCase;
    }

    @Override
    @MerchantTransactional
    public MerchantMemberResult handle(RemoveMerchantMemberCommand command) {
        return removeMerchantMemberUseCase.execute(command);
    }

    @Override
    public Class<RemoveMerchantMemberCommand> getCommandType() {
        return RemoveMerchantMemberCommand.class;
    }
}
