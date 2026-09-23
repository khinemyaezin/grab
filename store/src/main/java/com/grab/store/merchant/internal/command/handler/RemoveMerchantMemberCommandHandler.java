package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RemoveMerchantMemberCommand;
import com.merchant.application.port.inbound.RemoveMerchantMemberUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveMerchantMemberCommandHandler implements CommandHandler<RemoveMerchantMemberCommand, MerchantMemberResult> {

    private final RemoveMerchantMemberUseCase removeMerchantMemberUseCase;

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
