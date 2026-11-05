package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.RemoveSellerMerchantMemberCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class RemoveSellerMerchantMemberCommandHandler
        implements CommandHandler<RemoveSellerMerchantMemberCommand, Void> {

    private final SellerMerchantOutboundPort outboundPort;

    public RemoveSellerMerchantMemberCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(RemoveSellerMerchantMemberCommand command) {
        outboundPort.removeMember(command.merchantId(), command.memberId(), command.actorId());
        return null;
    }

    @Override
    public Class<RemoveSellerMerchantMemberCommand> getCommandType() {
        return RemoveSellerMerchantMemberCommand.class;
    }
}

