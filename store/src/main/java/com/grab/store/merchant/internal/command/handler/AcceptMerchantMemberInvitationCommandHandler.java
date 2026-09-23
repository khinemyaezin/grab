package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.model.write.AcceptMerchantMemberInvitationCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.port.inbound.AcceptMerchantMemberInvitationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AcceptMerchantMemberInvitationCommandHandler implements CommandHandler<AcceptMerchantMemberInvitationCommand, MerchantMemberResult> {

    private final AcceptMerchantMemberInvitationUseCase acceptMerchantMemberInvitationUseCase;

    @Override
    @MerchantTransactional
    public MerchantMemberResult handle(AcceptMerchantMemberInvitationCommand command) {
        return acceptMerchantMemberInvitationUseCase.execute(command);
    }

    @Override
    public Class<AcceptMerchantMemberInvitationCommand> getCommandType() {
        return AcceptMerchantMemberInvitationCommand.class;
    }
}
