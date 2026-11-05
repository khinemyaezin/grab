package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.AcceptMerchantMemberInvitationCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.port.inbound.AcceptMerchantMemberInvitationUseCase;
import org.springframework.stereotype.Component;

@Component
public class AcceptMerchantMemberInvitationCommandHandler
        implements CommandHandler<AcceptMerchantMemberInvitationCommand, MerchantMemberResult> {

    private final AcceptMerchantMemberInvitationUseCase acceptMerchantMemberInvitationUseCase;

    public AcceptMerchantMemberInvitationCommandHandler(
            AcceptMerchantMemberInvitationUseCase acceptMerchantMemberInvitationUseCase) {
        this.acceptMerchantMemberInvitationUseCase = acceptMerchantMemberInvitationUseCase;
    }

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

