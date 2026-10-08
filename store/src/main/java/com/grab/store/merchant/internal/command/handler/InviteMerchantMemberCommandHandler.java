package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.model.write.InviteMerchantMemberCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.port.inbound.InviteMerchantMemberUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InviteMerchantMemberCommandHandler implements CommandHandler<InviteMerchantMemberCommand, MerchantMemberResult> {

    private final InviteMerchantMemberUseCase inviteMerchantMemberUseCase;

    @Override
    @MerchantTransactional
    public MerchantMemberResult handle(InviteMerchantMemberCommand command) {
        return inviteMerchantMemberUseCase.execute(command);
    }

    @Override
    public Class<InviteMerchantMemberCommand> getCommandType() {
        return InviteMerchantMemberCommand.class;
    }
}
