package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.AcceptSellerMerchantInvitationCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class AcceptSellerMerchantInvitationCommandHandler
        implements CommandHandler<AcceptSellerMerchantInvitationCommand, MerchantMemberOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public AcceptSellerMerchantInvitationCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantMemberOutboundResult handle(AcceptSellerMerchantInvitationCommand command) {
        return outboundPort.acceptInvitation(command.merchantId(), command.memberId(), command.actorId());
    }

    @Override
    public Class<AcceptSellerMerchantInvitationCommand> getCommandType() {
        return AcceptSellerMerchantInvitationCommand.class;
    }
}

