package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.InviteSellerMerchantMemberCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.InviteMemberRecord;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class InviteSellerMerchantMemberCommandHandler
        implements CommandHandler<InviteSellerMerchantMemberCommand, MerchantMemberOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public InviteSellerMerchantMemberCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantMemberOutboundResult handle(InviteSellerMerchantMemberCommand command) {
        InviteMemberRecord record = new InviteMemberRecord(
                command.merchantId(),
                command.actorId(),
                command.targetUserId(),
                command.role(),
                command.expiresAt()
        );
        return outboundPort.inviteMember(record);
    }

    @Override
    public Class<InviteSellerMerchantMemberCommand> getCommandType() {
        return InviteSellerMerchantMemberCommand.class;
    }
}

