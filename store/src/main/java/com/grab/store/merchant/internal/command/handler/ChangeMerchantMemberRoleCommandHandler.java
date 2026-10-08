package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.model.write.ChangeMerchantMemberRoleCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.port.inbound.ChangeMerchantMemberRoleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeMerchantMemberRoleCommandHandler implements CommandHandler<ChangeMerchantMemberRoleCommand, MerchantMemberResult> {

    private final ChangeMerchantMemberRoleUseCase changeMerchantMemberRoleUseCase;

    @Override
    @MerchantTransactional
    public MerchantMemberResult handle(ChangeMerchantMemberRoleCommand command) {
        return changeMerchantMemberRoleUseCase.execute(command);
    }

    @Override
    public Class<ChangeMerchantMemberRoleCommand> getCommandType() {
        return ChangeMerchantMemberRoleCommand.class;
    }
}
